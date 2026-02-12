package com.nhb.autoconfigure.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/11 15:25
 * @description:
 */
@Slf4j
public class InterNetUtil {
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private final Properties properties;
    private final ExecutorService executorService;

    public InterNetUtil() {
        this(new Properties());
    }

    public InterNetUtil(Properties properties) {
        this.properties = properties;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "inet-util");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 获取主机信息
     */
    public HostInfo findFirstNonLoopbackHostInfo() {
        InetAddress address = findFirstNonLoopbackAddress();
        if (address != null) {
            return convertAddress(address);
        }
        return new HostInfo("unknown", "unknown");
    }

    /**
     * 查找第一个非回环地址
     */
    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    int current = calculatePriority(ifc);
                    if (current < lowest || result == null) {
                        lowest = current;
                    } else {
                        continue;
                    }
                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses();
                         addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        if (isPreferredAddress(address)) {
                            result = address;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取所有非回环地址
     */
    public List<InetAddress> getAllNonLoopbackAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp() && !ignoreInterface(ifc.getDisplayName())) {
                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses();
                         addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        if (!address.isLoopbackAddress() && isPreferredAddress(address)) {
                            addresses.add(address);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        return addresses;
    }

    /**
     * 通过主机名查找地址（带超时）
     */
    public InetAddress findAddressByHostname(String hostname) throws UnknownHostException {
        try {
            Future<InetAddress> future = executorService.submit(() -> {
                try {
                    return InetAddress.getByName(hostname);
                } catch (UnknownHostException e) {
                    throw e;
                }
            });
            return future.get(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UnknownHostException("Interrupted while resolving hostname: " + hostname);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownHostException) {
                throw (UnknownHostException) e.getCause();
            }
            throw new UnknownHostException("Error resolving hostname: " + hostname);
        } catch (TimeoutException e) {
            throw new UnknownHostException("Timeout resolving hostname: " + hostname);
        }
    }

    /**
     * 获取本机IP地址
     */
    public String getLocalIpAddress() {
        HostInfo hostInfo = findFirstNonLoopbackHostInfo();
        return hostInfo.getIpAddress();
    }

    /**
     * 获取本机主机名
     */
    public String getLocalHostname() {
        HostInfo hostInfo = findFirstNonLoopbackHostInfo();
        return hostInfo.getHostname();
    }

    private boolean isPreferredAddress(InetAddress address) {
        if (address.isLoopbackAddress()) {
            return false;
        }
        if (!properties.isUseOnlySiteLocalInterfaces() && address.isSiteLocalAddress()) {
            return true;
        }
        if (!properties.isPreferIpv4() && address instanceof Inet6Address) {
            return true;
        }
        return address instanceof Inet4Address;
    }

    private int calculatePriority(NetworkInterface ifc) throws SocketException {
        int priority = 0;
        if (ifc.isVirtual() || ifc.isPointToPoint()) {
            priority += 2;
        }
        String displayName = ifc.getDisplayName();
        if (ignoreInterface(displayName)) {
            priority += 4;
        }
        return priority;
    }

    private boolean ignoreInterface(String interfaceName) {
        if (interfaceName == null) {
            return false;
        }
        for (String regex : properties.getIgnoredInterfaces()) {
            if (interfaceName.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    private HostInfo convertAddress(InetAddress address) {
        String hostname;
        String ip;
        if (properties.isReverseLookup()) {
            hostname = address.getHostName();
        } else {
            hostname = address.getHostAddress();
        }
        ip = address.getHostAddress();
        return new HostInfo(hostname, ip);
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * 主机信息类
     */
    @Data
    public static class HostInfo {
        private final String hostname;
        private final String ipAddress;

        public HostInfo(String hostname, String ipAddress) {
            this.hostname = hostname;
            this.ipAddress = ipAddress;
        }
    }

    /**
     * 配置属性类
     */
    @Data
    public static class Properties {
        private boolean useOnlySiteLocalInterfaces = false;
        private boolean preferIpv4 = true;
        private boolean reverseLookup = false;
        private int timeoutSeconds = 1;
        private Set<String> ignoredInterfaces = new LinkedHashSet<>();
        private Set<String> preferredNetworks = new HashSet<>();

        public Set<String> getIgnoredInterfaces() {
            return Collections.unmodifiableSet(ignoredInterfaces);
        }
    }
}
