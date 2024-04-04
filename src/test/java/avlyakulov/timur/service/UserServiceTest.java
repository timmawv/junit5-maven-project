package avlyakulov.timur.service;

import avlyakulov.timur.TestBase;
import avlyakulov.timur.dao.UserDao;
import avlyakulov.timur.dto.User;
import avlyakulov.timur.extension.ConditionalExtension;
import avlyakulov.timur.extension.PostProcessingExtension;
import avlyakulov.timur.extension.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
@Tag("user")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
//        ThrowableExtension.class,
})
public class UserServiceTest extends TestBase {

    private static final User TIMUR = User.of(1, "Timur", "123");

    private static final User DIMA = User.of(2, "Dima", "111");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;


    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        this.userDao = Mockito.spy(UserDao.class);
//        this.userService = new UserService(userDao);
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(TIMUR);
//        Mockito.doReturn(true).when(userDao).delete(TIMUR.getId());
        Mockito.doReturn(true).when(userDao).delete(Mockito.any());
//        Mockito.when(userDao.delete(TIMUR.getId()))
//                .thenReturn(true)
//                .thenReturn(false);
        boolean deleteResult = userService.delete(TIMUR.getId());

        Mockito.verify(userDao, Mockito.times(1)).delete(argumentCaptor.capture());//по умолчанию будет проверять что было вызвано 1 раз

        assertThat(argumentCaptor.getValue()).isEqualTo(1);

        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    @DisplayName("user will be empty if no user added")
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertThat(users).isEmpty();
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded(UserService userService) {
        System.out.println("Test 2: " + this);
        userService.add(TIMUR, DIMA);
        List<User> users = userService.getAll();
        assertThat(users).hasSize(2);
    }


    @Test
    void userConvertedToMapById() {
        userService.add(TIMUR, DIMA);

        Map<Integer, User> users = userService.getAllConvertedById();

        //здесь оба assert запустяться даже если 1 упадет.
        assertAll(
                () -> assertThat(users).containsKeys(TIMUR.getId(), DIMA.getId()),
                () -> assertThat(users).containsValues(TIMUR, DIMA)
        );
    }

    @Nested
    @Tag("login")
    @DisplayName("test user login functionality")
    class LoginTest {
        @Test
        void loginSuccessIfUserExists() {
            userService.add(TIMUR);
            Optional<User> maybeUser = userService.login(TIMUR.getUsername(), TIMUR.getPassword());

            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(TIMUR));
        }

        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> {
                        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
            );
        }

        @Test
        void loginFailedIfPasswordNotCorrect() {
            userService.add(TIMUR);

            Optional<User> maybeUser = userService.login(TIMUR.getUsername(), "dummy");

            assertThat(maybeUser).isEmpty();
        }

        //        @Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailedIfUserNotExist() {
            userService.add(TIMUR);

            Optional<User> maybeUser = userService.login("dummy", TIMUR.getPassword());

            assertThat(maybeUser).isEmpty();
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            Optional<User> result = assertTimeout(Duration.ofMillis(200L), () -> {
                return userService.login("dummy", TIMUR.getPassword());
            });
        }


        @ParameterizedTest(name = "{arguments} test")
        //@ArgumentProvider
//        @NullSource//все эти провайдеры используются для 1 аргумента который передается в нашем случае их 2
//        @EmptySource
//        @NullAndEmptySource
//        @ValueSource(strings = {
//                "Timur", "Dima"
//        })
        @MethodSource("avlyakulov.timur.service.UserServiceTest#getArgumentsForLoginTests")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({
//                "Timur,123",
//                "Dima,111"
//        })
        @DisplayName("login param test")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(TIMUR, DIMA);

            Optional<User> maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTests() {
        return Stream.of(
                Arguments.of("Timur", "123", Optional.of(TIMUR)),
                Arguments.of("Dima", "111", Optional.of(DIMA)),
                Arguments.of("Dima", "dummy", Optional.empty()),
                Arguments.of("dummy", "111", Optional.empty())
        );
    }
}