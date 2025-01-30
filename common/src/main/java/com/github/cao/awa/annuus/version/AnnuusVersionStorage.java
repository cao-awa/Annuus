package com.github.cao.awa.annuus.version;

public interface AnnuusVersionStorage {
    default int getAnnuusVersion() {
        return annuus$getAnnuusVersion();
    }
    int annuus$getAnnuusVersion();
    default int setAnnuusVersion(int version) {
        return annuus$setAnnuusVersion(version);
    }
    int annuus$setAnnuusVersion(int version);
}
