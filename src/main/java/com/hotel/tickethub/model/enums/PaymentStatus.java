package com.hotel.tickethub.model.enums;

/**
 * Statut des paiements
 */
public enum PaymentStatus {
    PAID("Payé"),
    PENDING("En attente"),
    OVERDUE("En retard"),
    FAILED("Échoué"),
    CANCELLED("Annulé");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
