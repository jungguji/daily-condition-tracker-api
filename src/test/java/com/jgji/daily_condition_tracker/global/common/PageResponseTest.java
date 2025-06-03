package com.jgji.daily_condition_tracker.global.common;

import com.jgji.daily_condition_tracker.domain.shared.presentation.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @DisplayName("PageResponse 생성 - 성공 케이스")
    @Nested
    class Success {

        @DisplayName("다양한 페이지네이션 시나리오 테스트")
        @ParameterizedTest(name = "{index} => {0}: page={1}, size={2}, totalElements={3}")
        @MethodSource("getPageResponseScenarios")
        void createPageResponseWithVariousScenarios(String scenario, int page, int size, long totalElements, 
                                                   int contentSize, int expectedTotalPages, 
                                                   boolean expectedFirst, boolean expectedLast, boolean expectedEmpty) {
            List<String> content = createContentList(contentSize);
            
            PageResponse<String> pageResponse = PageResponse.of(content, page, size, totalElements);

            assertThat(pageResponse.content()).hasSize(contentSize);

            assertThat(pageResponse.page()).isEqualTo(page);
            assertThat(pageResponse.size()).isEqualTo(size);
            assertThat(pageResponse.totalElements()).isEqualTo(totalElements);
            assertThat(pageResponse.totalPages()).isEqualTo(expectedTotalPages);

            assertThat(pageResponse.first()).isEqualTo(expectedFirst);
            assertThat(pageResponse.last()).isEqualTo(expectedLast);
            assertThat(pageResponse.empty()).isEqualTo(expectedEmpty);
        }

        static Stream<Object[]> getPageResponseScenarios() {
            return Stream.of(
                    new Object[]{"첫 번째 페이지", 0, 10, 25L, 3, 3, true, false, false},
                    new Object[]{"마지막 페이지", 2, 10, 22L, 2, 3, false, true, false},
                    new Object[]{"중간 페이지", 1, 5, 15L, 5, 3, false, false, false},
                    new Object[]{"빈 페이지", 0, 10, 0L, 0, 0, true, true, true},
                    new Object[]{"단일 페이지", 0, 10, 3L, 3, 1, true, true, false}
            );
        }

        private List<String> createContentList(int size) {
            if (size == 0) {
                return Collections.emptyList();
            }
            return java.util.stream.IntStream.range(1, size + 1)
                    .mapToObj(i -> "item" + i)
                    .toList();
        }
    }

    @DisplayName("PageResponse 경계값 테스트")
    @Nested
    class BoundaryTest {

        @DisplayName("다양한 경계값 시나리오 테스트")
        @ParameterizedTest(name = "{index} => {0}")
        @MethodSource("getBoundaryTestScenarios")
        void testBoundaryScenarios(String scenario, int page, int size, long totalElements,
                                 int contentSize, int expectedTotalPages, 
                                 boolean expectedFirst, boolean expectedLast) {
            List<String> content = createContentList(contentSize);
            
            PageResponse<String> pageResponse = PageResponse.of(content, page, size, totalElements);

            assertThat(pageResponse.totalPages()).isEqualTo(expectedTotalPages);
            assertThat(pageResponse.totalElements()).isEqualTo(totalElements);

            assertThat(pageResponse.first()).isEqualTo(expectedFirst);
            assertThat(pageResponse.last()).isEqualTo(expectedLast);
            assertThat(pageResponse.page()).isEqualTo(page);
            assertThat(pageResponse.size()).isEqualTo(size);
        }

        static Stream<Object[]> getBoundaryTestScenarios() {
            return Stream.of(
                    new Object[]{"총 요소 수가 페이지 크기로 나누어떨어지는 경우", 1, 5, 10L, 5, 2, false, true},
                    new Object[]{"총 요소 수가 페이지 크기로 나누어떨어지지 않는 경우", 2, 5, 13L, 3, 3, false, true},
                    new Object[]{"페이지 크기가 1인 경우", 2, 1, 5L, 1, 5, false, false},
                    new Object[]{"페이지 크기가 총 요소 수보다 큰 경우", 0, 10, 2L, 2, 1, true, true}
            );
        }

        private List<String> createContentList(int size) {
            return java.util.stream.IntStream.range(1, size + 1)
                    .mapToObj(i -> "item" + i)
                    .toList();
        }
    }
} 