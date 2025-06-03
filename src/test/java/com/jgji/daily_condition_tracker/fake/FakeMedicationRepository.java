package com.jgji.daily_condition_tracker.fake;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.domain.MedicationRepository;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

public class FakeMedicationRepository implements MedicationRepository {

    private final Map<Long, Medication> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build();

    @Override
    public Medication save(Medication medication) {
        if (medication.getMedicationId() == null) {
            long newId = idGenerator.getAndIncrement();

            Medication newMedication = fixtureMonkey.giveMeBuilder(Medication.class)
                    .set(javaGetter(Medication::getMedicationId), newId)
                    .set("userId", medication.getUserId())
                    .set("name", medication.getName())
                    .set("dosage", medication.getDosage())
                    .set("unit", medication.getUnit())
                    .set("description", medication.getDescription())
                    .set("isActive", medication.isActive())
                    .set("isDeleted", false)
                    .sample();

            store.put(newId, newMedication);
            return newMedication;
        } else {
            store.put(medication.getMedicationId(), medication);
            return medication;
        }
    }

    @Override
    public Optional<Medication> findByIdAndUserId(long medicationId, long userId) {
        return Optional.ofNullable(store.get(medicationId))
                .filter(medication -> medication.getUserId() == userId && !medication.isDeleted());
    }

    @Override
    public Page<Medication> findByUserIdAndIsActive(long userId, boolean isActive, Pageable pageable) {
        List<Medication> filtered = store.values().stream()
                .filter(medication -> medication.getUserId() == userId && 
                                    !medication.isDeleted() && 
                                    medication.isActive() == isActive)
                .collect(Collectors.toList());
        
        return createPage(filtered, pageable);
    }

    @Override
    public Page<Medication> findByUserId(long userId, Pageable pageable) {
        List<Medication> filtered = store.values().stream()
                .filter(medication -> medication.getUserId() == userId && !medication.isDeleted())
                .collect(Collectors.toList());
        
        return createPage(filtered, pageable);
    }

    @Override
    public boolean existsByNameAndUserIdAndIdNot(String name, long userId, long medicationId) {
        return store.values().stream()
                .anyMatch(medication -> medication.getName().equals(name) && 
                                      medication.getUserId() == userId && 
                                      medication.getMedicationId() != medicationId &&
                                      !medication.isDeleted());
    }

    private Page<Medication> createPage(List<Medication> medications, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), medications.size());
        
        if (start >= medications.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, medications.size());
        }
        
        List<Medication> pageContent = medications.subList(start, end);
        return new PageImpl<>(pageContent, pageable, medications.size());
    }

    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    public void saveAll(List<Medication> medications) {
        medications.forEach(this::save);
    }
}
