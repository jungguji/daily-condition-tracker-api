package com.jgji.daily_condition_tracker.domain.medication.application;

import com.jgji.daily_condition_tracker.domain.medication.domain.Medication;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationCreateRequest;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationResponse;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationSummaryResponse;
import com.jgji.daily_condition_tracker.domain.medication.presentation.dto.MedicationUpdateRequest;
import com.jgji.daily_condition_tracker.fake.FakeMedicationRepository;
import com.jgji.daily_condition_tracker.domain.shared.presentation.dto.PageRequest;
import com.jgji.daily_condition_tracker.domain.shared.presentation.dto.PageResponse;
import com.jgji.daily_condition_tracker.global.exception.BusinessRuleViolationException;
import com.jgji.daily_condition_tracker.global.exception.ResourceNotFoundException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicationServiceTest {

    private FakeMedicationRepository medicationRepository;
    private MedicationService medicationService;
    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setUp() {
        medicationRepository = new FakeMedicationRepository();
        medicationService = new MedicationService(medicationRepository);
        fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
                .build();
    }

    @DisplayName("약물 생성")
    @Nested
    class CreateMedication {

        @DisplayName("약물 생성 성공")
        @ParameterizedTest(name = "{index} => 사용자ID={0}, 이름={1}, 용량={2}, 단위={3}, 활성상태={4}")
        @MethodSource("createMedicationSuccessScenarios")
        void createMedicationSuccess(long userId, String name, Integer dosage, String unit, String description, boolean isActive) {
            MedicationCreateRequest request = new MedicationCreateRequest(name, dosage, unit, description, isActive);

            MedicationResponse response = medicationService.createMedication(userId, request);

            assertThat(response.name()).isEqualTo(name);
            assertThat(response.dosage()).isEqualTo(dosage);
            assertThat(response.unit()).isEqualTo(unit);
            assertThat(response.description()).isEqualTo(description);
            assertThat(response.isActive()).isEqualTo(isActive);
        }

        static Stream<Arguments> createMedicationSuccessScenarios() {
            return Stream.of(
                    Arguments.of(1L, "아스피린", 500, "mg", "해열진통제", true),
                    Arguments.of(2L, "타이레놀", 1, "정", "진통제", false),
                    Arguments.of(1L, "비타민C", null, null, null, true),
                    Arguments.of(3L, "오메가3", 1000, "mg", "건강기능식품", true)
            );
        }

        @DisplayName("약물 생성 실패 - 이름 검증")
        @ParameterizedTest(name = "{index} => 이름={0}")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void createMedicationFailWithInvalidName(String invalidName) {
            MedicationCreateRequest request = new MedicationCreateRequest(invalidName, 500, "mg", "테스트", true);

            assertThatThrownBy(() -> medicationService.createMedication(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("약 이름은 필수값입니다.");
        }

        @DisplayName("약물 생성 실패 - null 이름")
        @Test
        void createMedicationFailWithNullName() {
            MedicationCreateRequest request = new MedicationCreateRequest(null, 500, "mg", "테스트", true);

            assertThatThrownBy(() -> medicationService.createMedication(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("약 이름은 필수값입니다.");
        }
    }

    @DisplayName("약물 목록 조회")
    @Nested
    class FindMedicationsByUserId {

        @BeforeEach
        void setUpMedications() {
            medicationRepository.clear();
            List<Medication> medications = List.of(
                    createTestMedication(1L, "아스피린", true),
                    createTestMedication(1L, "타이레놀", false),
                    createTestMedication(1L, "비타민C", true),
                    createTestMedication(2L, "오메가3", true)
            );
            medicationRepository.saveAll(medications);
        }

        @DisplayName("전체 약물 목록 조회")
        @ParameterizedTest(name = "{index} => 페이지={0}, 조회된 데이터 갯수={1}")
        @MethodSource("pageRequestScenarios")
        void findAllMedicationsByUserId(int page, int size) {
            PageRequest pageRequest = PageRequest.of(page, size, "medicationId", "desc");

            PageResponse<MedicationSummaryResponse> response = 
                    medicationService.findMedicationsByUserId(1L, pageRequest, null);

            assertThat(response.content()).hasSize(Math.min(size, 3));
            assertThat(response.totalElements()).isEqualTo(3);
            assertThat(response.page()).isEqualTo(page);
            assertThat(response.size()).isEqualTo(size);
        }

        static Stream<Arguments> pageRequestScenarios() {
            return Stream.of(
                    Arguments.of(0, 10),
                    Arguments.of(0, 2),
                    Arguments.of(1, 1)
            );
        }

        @DisplayName("활성 상태별 약물 목록 조회")
        @ParameterizedTest(name = "{index} => 활성상태={0}, 예상개수={1}")
        @MethodSource("activeStatusScenarios")
        void findMedicationsByActiveStatus(Boolean isActive, int expectedCount) {
            PageRequest pageRequest = PageRequest.of(0, 10, "medicationId", "desc");

            PageResponse<MedicationSummaryResponse> response = 
                    medicationService.findMedicationsByUserId(1L, pageRequest, isActive);

            assertThat(response.totalElements()).isEqualTo(expectedCount);
            if (isActive != null) {
                response.content().forEach(medication -> 
                        assertThat(medication.isActive()).isEqualTo(isActive));
            }
        }

        static Stream<Arguments> activeStatusScenarios() {
            return Stream.of(
                    Arguments.of(true, 2),
                    Arguments.of(false, 1),
                    Arguments.of(null, 3)
            );
        }

        @Test
        @DisplayName("다른 사용자 약물은 조회되지 않음")
        void findMedicationsForDifferentUser() {
            PageRequest pageRequest = PageRequest.of(0, 10, "medicationId", "desc");

            PageResponse<MedicationSummaryResponse> response = 
                    medicationService.findMedicationsByUserId(999L, pageRequest, null);

            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isZero();
        }
    }

    @DisplayName("약물 상세 조회")
    @Nested
    class FindMedicationById {

        @Test
        @DisplayName("약물 상세 조회 성공")
        void findMedicationByIdSuccess() {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            MedicationResponse response = medicationService.findMedicationById(savedMedication.getMedicationId(), 1L);

            assertThat(response.medicationId()).isEqualTo(savedMedication.getMedicationId());
            assertThat(response.name()).isEqualTo("아스피린");
        }

        @Test
        @DisplayName("존재하지 않는 약물 조회 실패")
        void findNonExistentMedication() {
            assertThatThrownBy(() -> medicationService.findMedicationById(999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("약");
        }

        @Test
        @DisplayName("다른 사용자 약물 조회 실패")
        void findMedicationByDifferentUser() {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            assertThatThrownBy(() -> medicationService.findMedicationById(savedMedication.getMedicationId(), 2L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("약");
        }
    }

    @DisplayName("약물 수정")
    @Nested
    class UpdateMedication {

        @Test
        @DisplayName("약물 수정 성공")
        void updateMedicationSuccess() {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            MedicationUpdateRequest request = new MedicationUpdateRequest(
                    JsonNullable.of("타이레놀"),
                    JsonNullable.of(1000),
                    JsonNullable.of("정"),
                    JsonNullable.of("업데이트된 설명"),
                    JsonNullable.of(false)
            );

            MedicationResponse response = medicationService.updateMedication(1L, savedMedication.getMedicationId(), request);

            assertThat(response.name()).isEqualTo("타이레놀");
            assertThat(response.dosage()).isEqualTo(1000);
            assertThat(response.unit()).isEqualTo("정");
            assertThat(response.description()).isEqualTo("업데이트된 설명");
            assertThat(response.isActive()).isFalse();
        }

        @DisplayName("부분 수정 성공")
        @ParameterizedTest(name = "{index} => {0}")
        @MethodSource("partialUpdateScenarios")
        void partialUpdateSuccess(String description, MedicationUpdateRequest request, String expectedName, Integer expectedDosage) {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            MedicationResponse response = medicationService.updateMedication(1L, savedMedication.getMedicationId(), request);

            assertThat(response.name()).isEqualTo(expectedName);
            assertThat(response.dosage()).isEqualTo(expectedDosage);
        }

        static Stream<Arguments> partialUpdateScenarios() {
            return Stream.of(
                    Arguments.of(
                            "이름만 수정",
                            new MedicationUpdateRequest(JsonNullable.of("타이레놀"), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined()),
                            "타이레놀", 500
                    ),
                    Arguments.of(
                            "용량만 수정",
                            new MedicationUpdateRequest(JsonNullable.undefined(), JsonNullable.of(1000), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined()),
                            "아스피린", 1000
                    )
            );
        }

        @Test
        @DisplayName("모든 필드 undefined시 실패")
        void updateMedicationFailWithAllUndefined() {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            MedicationUpdateRequest request = new MedicationUpdateRequest(
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined()
            );

            assertThatThrownBy(() -> medicationService.updateMedication(1L, savedMedication.getMedicationId(), request))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("수정할 내용이 없습니다.");
        }

        @Test
        @DisplayName("이름 중복시 실패")
        void updateMedicationFailWithDuplicateName() {
            Medication medication1 = createTestMedication(1L, "아스피린", true);
            Medication medication2 = createTestMedication(1L, "타이레놀", true);
            Medication savedMedication1 = medicationRepository.save(medication1);
            medicationRepository.save(medication2);

            MedicationUpdateRequest request = new MedicationUpdateRequest(
                    JsonNullable.of("타이레놀"),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined()
            );

            assertThatThrownBy(() -> medicationService.updateMedication(1L, savedMedication1.getMedicationId(), request))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("이미 동일한 이름의 약물이 존재합니다");
        }

        @Test
        @DisplayName("존재하지 않는 약물 수정 실패")
        void updateNonExistentMedication() {
            MedicationUpdateRequest request = new MedicationUpdateRequest(
                    JsonNullable.of("타이레놀"),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined()
            );

            assertThatThrownBy(() -> medicationService.updateMedication(1L, 999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("약");
        }
    }

    @DisplayName("약물 삭제")
    @Nested
    class DeleteMedication {

        @Test
        @DisplayName("약물 삭제 성공")
        void deleteMedicationSuccess() {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            medicationService.deleteMedication(1L, savedMedication.getMedicationId());

            assertThatThrownBy(() -> medicationService.findMedicationById(savedMedication.getMedicationId(), 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("존재하지 않는 약물 삭제 실패")
        void deleteNonExistentMedication() {
            assertThatThrownBy(() -> medicationService.deleteMedication(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("약");
        }

        @Test
        @DisplayName("다른 사용자 약물 삭제 실패")
        void deleteMedicationByDifferentUser() {
            Medication medication = createTestMedication(1L, "아스피린", true);
            Medication savedMedication = medicationRepository.save(medication);

            assertThatThrownBy(() -> medicationService.deleteMedication(2L, savedMedication.getMedicationId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("약");
        }
    }

    private Medication createTestMedication(long userId, String name, boolean isActive) {
        return fixtureMonkey.giveMeBuilder(Medication.class)
                .set("userId", userId)
                .set("name", name)
                .set("dosage", 500)
                .set("unit", "mg")
                .set("description", "테스트 약물")
                .set("isActive", isActive)
                .set("isDeleted", false)
                .setNull("deletedAt")
                .sample();
    }
}