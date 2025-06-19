package com.example.javalabaip.util;

import java.util.regex.Pattern;

public class IpAddressValidator {
    private static final IpAddressValidator INSTANCE = new IpAddressValidator();
    private static final Pattern IP_PATTERN = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");

    private IpAddressValidator() {
    }

    public static IpAddressValidator getInstance() {
        return INSTANCE;
    }

    public boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && IP_PATTERN.matcher(ipAddress).matches();
    }
}