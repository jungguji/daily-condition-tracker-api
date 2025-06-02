package com.jgji.daily_condition_tracker.domain.medication.domain;

import com.jgji.daily_condition_tracker.constants.MedicationConstants;
import com.jgji.daily_condition_tracker.domain.medication.infrastructure.MedicationEntity;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicationTest {

    FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
            .build();

    @DisplayName("약물 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("약물 생성 - 모든 필드 포함")
        @Test
        void createMedicationWithAllFields() {
            Medication medication = Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.DEFAULT_NAME,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE
            );

            assertThat(medication.getUserId()).isEqualTo(MedicationConstants.DEFAULT_USER_ID);
            assertThat(medication.getName()).isEqualTo(MedicationConstants.DEFAULT_NAME);
            assertThat(medication.getDosage()).isEqualTo(MedicationConstants.DEFAULT_DOSAGE);
            assertThat(medication.getUnit()).isEqualTo(MedicationConstants.DEFAULT_UNIT);
            assertThat(medication.getDescription()).isEqualTo(MedicationConstants.DEFAULT_DESCRIPTION);
            assertThat(medication.isActive()).isEqualTo(MedicationConstants.DEFAULT_IS_ACTIVE);
            assertThat(medication.getCreatedAt()).isNotNull();
            assertThat(medication.getUpdatedAt()).isNotNull();
        }

        @DisplayName("약물 생성 - 필수 필드만")
        @Test
        void createMedicationWithRequiredFieldsOnly() {
            Medication medication = Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.DEFAULT_NAME,
                    null,
                    null,
                    null,
                    true
            );

            assertThat(medication.getUserId()).isEqualTo(MedicationConstants.DEFAULT_USER_ID);
            assertThat(medication.getName()).isEqualTo(MedicationConstants.DEFAULT_NAME);
            assertThat(medication.getDosage()).isNull();
            assertThat(medication.getUnit()).isNull();
            assertThat(medication.getDescription()).isNull();
            assertThat(medication.isActive()).isTrue();
        }

        @DisplayName("약물 생성 - 경계값 테스트 (이름 255자)")
        @Test
        void createMedicationWithMaxLengthName() {
            Medication medication = Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.VALID_NAME_255_CHARS,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE
            );

            assertThat(medication.getName()).isEqualTo(MedicationConstants.VALID_NAME_255_CHARS);
            assertThat(medication.getName()).hasSize(255);
        }

        @DisplayName("약물 생성 - 비활성 상태로 생성")
        @Test
        void createInactiveMedication() {
            Medication medication = Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.DEFAULT_NAME,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    false
            );

            assertThat(medication.isActive()).isFalse();
        }
    }

    @DisplayName("약물 생성 - 실패 케이스")
    @Nested
    class Fail {

        @DisplayName("약물 생성 - 이름이 null인 경우")
        @Test
        void createMedicationWithNullName() {
            assertThatThrownBy(() -> Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    null,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("약 이름은 필수값입니다.");
        }

        @DisplayName("약물 생성 - 이름이 빈 문자열인 경우")
        @ParameterizedTest(name = "{index} => name={0}")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void createMedicationWithEmptyName(String emptyName) {
            assertThatThrownBy(() -> Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    emptyName,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("약 이름은 필수값입니다.");
        }

        @DisplayName("약물 생성 - 이름이 255자 초과인 경우")
        @Test
        void createMedicationWithTooLongName() {
            assertThatThrownBy(() -> Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.INVALID_NAME_256_CHARS,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("약 이름은 최대 255자까지 입력 가능합니다.");
        }
    }

    @DisplayName("엔티티로부터 약물 객체 생성")
    @Nested
    class FromEntity {

        @DisplayName("엔티티로부터 약물 객체 생성 - 성공")
        @Test
        void createMedicationFromEntity() {
            MedicationEntity entity = fixtureMonkey.giveMeBuilder(MedicationEntity.class)
                    .set("medicationId", 1L)
                    .sample();

            Medication medication = Medication.of(entity);

            assertThat(medication.getMedicationId()).isEqualTo(entity.getMedicationId());
            assertThat(medication.getUserId()).isEqualTo(entity.getUserId());
            assertThat(medication.getName()).isEqualTo(entity.getName());
            assertThat(medication.getDosage()).isEqualTo(entity.getDosage());
            assertThat(medication.getUnit()).isEqualTo(entity.getUnit());
            assertThat(medication.getDescription()).isEqualTo(entity.getDescription());
            assertThat(medication.isActive()).isEqualTo(entity.isActive());
            assertThat(medication.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(medication.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @DisplayName("엔티티로부터 약물 객체 생성 - 선택적 필드 null")
        @Test
        void createMedicationFromEntityWithNullFields() {
            MedicationEntity entity = fixtureMonkey.giveMeBuilder(MedicationEntity.class)
                    .set("medicationId", 1L)
                    .set("userId", MedicationConstants.DEFAULT_USER_ID)
                    .set("name", MedicationConstants.DEFAULT_NAME)
                    .setNull("dosage")
                    .setNull("unit")
                    .setNull("description")
                    .set("isActive", true)
                    .set("createdAt", OffsetDateTime.now())
                    .set("updatedAt", OffsetDateTime.now())
                    .sample();

            Medication medication = Medication.of(entity);

            assertThat(medication.getMedicationId()).isEqualTo(entity.getMedicationId());
            assertThat(medication.getUserId()).isEqualTo(entity.getUserId());
            assertThat(medication.getName()).isEqualTo(entity.getName());
            assertThat(medication.getDosage()).isNull();
            assertThat(medication.getUnit()).isNull();
            assertThat(medication.getDescription()).isNull();
            assertThat(medication.isActive()).isTrue();
        }
    }

    @DisplayName("약물 객체 동일성 확인")
    @Nested
    class EqualityCheck {

        @DisplayName("동일한 내용으로 생성된 약물 객체 비교")
        @Test
        void compareMedicationsWithSameContent() {
            Medication medication1 = Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.DEFAULT_NAME,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE
            );

            Medication medication2 = Medication.create(
                    MedicationConstants.DEFAULT_USER_ID,
                    MedicationConstants.DEFAULT_NAME,
                    MedicationConstants.DEFAULT_DOSAGE,
                    MedicationConstants.DEFAULT_UNIT,
                    MedicationConstants.DEFAULT_DESCRIPTION,
                    MedicationConstants.DEFAULT_IS_ACTIVE
            );

            assertThat(medication1.getUserId()).isEqualTo(medication2.getUserId());
            assertThat(medication1.getName()).isEqualTo(medication2.getName());
            assertThat(medication1.getDosage()).isEqualTo(medication2.getDosage());
            assertThat(medication1.getUnit()).isEqualTo(medication2.getUnit());
            assertThat(medication1.getDescription()).isEqualTo(medication2.getDescription());
            assertThat(medication1.isActive()).isEqualTo(medication2.isActive());
        }
    }
} 