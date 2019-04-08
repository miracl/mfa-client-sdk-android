package com.miracl.mpinsdk.model;

/**
 * Verification type denotes how the MPinID is verified
 */
public enum VerificationType {
    /**
     * NONE verification type denotes the MPinID is not verified
     */
    NONE,
    /**
     * EMAIL verification type denotes the MPinID is verified through email
     */
    EMAIL,
    /**
     * REG_CODE verification type denotes the MPinID is verified through registration code
     */
    REG_CODE,
    /**
     * DVS verification type denotes the MPinID is DVS MPinID and verification was done on the auth MPinID
     */
    DVS,
    /**
     * PLUGGABLE verification type denotes custom process of verifying MPinID
     */
    PLUGGABLE
}
