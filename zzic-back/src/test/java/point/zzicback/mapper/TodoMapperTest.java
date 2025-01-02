package point.zzicback.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import point.zzicback.model.Todo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MybatisTest // MyBatis 테스트를 위한 어노테이션
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2를 기본 사용하도록 설정된 값을 변경하지 않도록 합니다.
class TodoMapperTest {

    @Autowired
    private TodoMapper todoMapper;

    @Nested
    @DisplayName("UPDATE TODO")
    class GetTodoList {

        @Test
        @DisplayName("성공적으로 Todo를 수정 함")
        void getTodoList_success() {
            // given
            Todo todo = new Todo();
            todo.setId(1L);
            todo.setDone(true);

            // when, 1번을 완료한것으로 수정
            int result = todoMapper.updateByPrimaryKeySelective(todo);

            // then 1, 기본 기능으로 검증
            assertTrue(result == 1); // 값이 1이어야 한다.

            // then 2, 수정된 Todo를 조회
            assertThat(result)
                    .withFailMessage("수정이 되지 않았습니다.") // 테스트케이스가 실패할 경우 콘솔에 힌트를 주기 위해 사용
                    .isOne(); // 값이 1이어야 한다.
        }

        @Test
        @DisplayName("잘못된 ID로 Todo를 수정 함")
        void getTodoList_fail() {
            // given
            Todo todo = new Todo();
            todo.setId(999999L);
            todo.setDone(true);

            // when, 999999번을 완료한것으로 수정
            int result = todoMapper.updateByPrimaryKeySelective(todo);

            // then 1, 기본 기능으로 검증
            assertTrue(result == 0); // 값이 0이어야 한다.

            // then 2, 고급 기능으로 확인 및 내용 출력
            assertThat(result)
                    .withFailMessage("ID가 잘못되었음에도 수정이 되었습니다.") // 테스트케이스가 실패할 경우 콘솔에 힌트를 주기 위해 사용
                    .isZero(); // 값이 0이어야 한다.
        }

    }
}