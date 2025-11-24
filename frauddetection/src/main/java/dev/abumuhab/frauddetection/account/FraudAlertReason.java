package dev.abumuhab.frauddetection.account;

public enum FraudAlertReason {
    HIGH_TRANSACTION_VELOCITY,
    HIGH_TRANSACTION_VALUE,
    SUSPICIOUS_SIGNUP,
    IMPOSSIBLE_GEOGRAPHIC_VELOCITY
}
