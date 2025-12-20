package com.hotel.tickethub.service;

import com.hotel.tickethub.model.HotelSubscription;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.model.Category;
import com.hotel.tickethub.repository.HotelSubscriptionRepository;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.PlanRepository;
import com.hotel.tickethub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelSubscriptionService {

    private final HotelSubscriptionRepository subscriptionRepository;
    private final HotelRepository hotelRepository;
    private final PlanRepository planRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Créer un nouvel abonnement pour un hôtel
     * Règle 3: Abonnements des hôtels
     */
    public HotelSubscription createSubscription(UUID hotelId, UUID planId) {
        Hotel hotel = hotelRepository.findById(hotelId)
            .orElseThrow(() -> new RuntimeException("Hôtel non trouvé"));

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        HotelSubscription subscription = HotelSubscription.builder()
            .hotel(hotel)
            .plan(plan)
            .additionalCategories(new HashSet<>())
            .startDate(LocalDateTime.now())
            .status("ACTIVE")
            .isPendingChange(false)
            .build();

        // Calculer fin de période (1 mois)
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));

        return subscriptionRepository.save(subscription);
    }

    /**
     * Obtenir l'abonnement actuel d'un hôtel
     */
    public Optional<HotelSubscription> getActiveSubscription(UUID hotelId) {
        return subscriptionRepository.findActiveSubscriptionByHotel(hotelId);
    }

    /**
     * Obtenir tout l'historique d'abonnement
     */
    public List<HotelSubscription> getSubscriptionHistory(UUID hotelId) {
        return subscriptionRepository.findByHotelIdOrderByStartDateDesc(hotelId);
    }

    /**
     * Ajouter une catégorie supplémentaire
     * Règle 3: Catégories supplémentaires moyennant frais
     */
    private static final String SUBSCRIPTION_NOT_FOUND_MESSAGE = "Abonnement non trouvé";

    public HotelSubscription addAdditionalCategory(UUID subscriptionId, UUID categoryId) {
        HotelSubscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

        subscription.getAdditionalCategories().add(category);
        return subscriptionRepository.save(subscription);
    }

    /**
     * Retirer une catégorie supplémentaire
     */
    public HotelSubscription removeAdditionalCategory(UUID subscriptionId, UUID categoryId) {
        HotelSubscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));

        subscription.getAdditionalCategories()
            .removeIf(c -> c.getId().equals(categoryId));

        return subscriptionRepository.save(subscription);
    }

    /**
     * Changer le plan d'un hôtel
     * Règle 9: Changement effectif au prochain cycle
     */
    public HotelSubscription changePlan(UUID subscriptionId, UUID newPlanId) {
        HotelSubscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException(SUBSCRIPTION_NOT_FOUND_MESSAGE));

        planRepository.findById(newPlanId)
            .orElseThrow(() -> new RuntimeException("Plan non trouvé"));

        // Marquer comme changement en attente
        subscription.setStatus("PENDING_CHANGE");
        subscription.setIsPendingChange(true);
        // Le nouveau plan est appliqué au prochain cycle (à gérer par scheduler)

        return subscriptionRepository.save(subscription);
    }

    /**
     * Appliquer les changements de plan en attente
     * À appeler par scheduler à la fin de la période
     */
    public void applyPendingPlanChanges() {
        subscriptionRepository.findPendingPlanChanges().forEach(subscription -> {
            // Créer un nouvel abonnement pour le nouveau cycle
            HotelSubscription newSubscription = HotelSubscription.builder()
                .hotel(subscription.getHotel())
                .plan(subscription.getPlan()) // Le nouveau plan
                .additionalCategories(new HashSet<>(subscription.getAdditionalCategories()))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .status("ACTIVE")
                .isPendingChange(false)
                .build();

            subscriptionRepository.save(newSubscription);

            // Fermer l'ancien
            subscription.setStatus("INACTIVE");
            subscriptionRepository.save(subscription);
        });
    }

    /**
     * Obtenir le nombre de techniciens utilisés
     * Pour vérifier si on dépasse la limite du plan
     */
    public int getTechnicianCount(UUID hotelId) {
        return hotelRepository.findById(hotelId)
            .map(hotel -> (int) hotel.getUsers().stream()
                .filter(user -> user.getUserRoles().stream()
                    .anyMatch(role -> "TECHNICIAN".equals(role.getRole().toString())))
                .count())
            .orElse(0);
    }

    /**
     * Vérifier si le quota de techniciens est atteint
     */
    public boolean isTechnicianQuotaReached(UUID hotelId) {
        Optional<HotelSubscription> subscription = getActiveSubscription(hotelId);
        if (subscription.isEmpty()) {
            return true; // Pas d'abonnement = quota atteint
        }

        int maxTechs = subscription.get().getPlan().getMaxTechnicians();
        int currentTechs = getTechnicianCount(hotelId);

        return currentTechs >= maxTechs;
    }
}
