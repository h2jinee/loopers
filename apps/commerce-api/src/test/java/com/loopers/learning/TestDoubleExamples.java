package com.loopers.learning;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test Double의 5가지 종류 예시
 * 
 * Test Double은 테스트를 위해 실제 객체를 대체하는 객체들의 총칭입니다.
 * Martin Fowler가 정의한 5가지 종류: Dummy, Stub, Spy, Mock, Fake
 */
public class TestDoubleExamples {

    // 공통으로 사용할 인터페이스들
    interface EmailService {
        void send(String to, String subject, String body);
        boolean isValidEmail(String email);
    }

    interface UserRepository {
        User save(User user);
        Optional<User> findById(Long id);
        List<User> findAll();
        void deleteById(Long id);
    }

    static class User {
        private Long id;
        private String name;
        private String email;

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // Getters and setters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public void setId(Long id) { this.id = id; }
    }

    static class UserService {
        private final UserRepository repository;
        private final EmailService emailService;

        public UserService(UserRepository repository, EmailService emailService) {
            this.repository = repository;
            this.emailService = emailService;
        }

        public User createUser(String name, String email) {
            User user = new User(null, name, email);
            User savedUser = repository.save(user);
            
            // 이메일 서비스는 사용하지만 테스트에서는 중요하지 않을 수 있음
            if (emailService != null && emailService.isValidEmail(email)) {
                emailService.send(email, "Welcome", "Welcome to our service!");
            }
            
            return savedUser;
        }

        public Optional<User> getUser(Long id) {
            return repository.findById(id);
        }
    }

    /**
     * 1. DUMMY - 전달만 되고 실제로 사용되지 않는 객체
     * 
     * 특징:
     * - 메서드 시그니처를 만족시키기 위해서만 존재
     * - 실제로 호출되지 않음
     * - 보통 null이나 빈 구현체 사용
     */
    @Test
    void dummyExample() {
        // Dummy EmailService - 실제로 사용되지 않음
        EmailService dummyEmailService = new EmailService() {
            @Override
            public void send(String to, String subject, String body) {
                throw new UnsupportedOperationException("This is a dummy!");
            }

            @Override
            public boolean isValidEmail(String email) {
                throw new UnsupportedOperationException("This is a dummy!");
            }
        };

        // 실제 동작하는 Repository (이 테스트의 관심사)
        UserRepository repository = new UserRepository() {
            @Override
            public User save(User user) {
                user.setId(1L);
                return user;
            }

            @Override
            public Optional<User> findById(Long id) {
                return Optional.empty();
            }

            @Override
            public List<User> findAll() {
                return Collections.emptyList();
            }

            @Override
            public void deleteById(Long id) {
            }
        };

        // EmailService를 null로 전달하거나 dummy를 전달
        UserService service = new UserService(repository, null); // 또는 dummyEmailService
        
        // 이메일 서비스는 null이므로 호출되지 않음
        User user = service.createUser("John", "john@example.com");
        
        assertThat(user.getId()).isEqualTo(1L);
    }

    /**
     * 2. STUB - 미리 준비된 답변을 제공하는 객체
     * 
     * 특징:
     * - 테스트에 필요한 최소한의 구현만 제공
     * - 항상 동일한 값을 반환
     * - 테스트 시나리오에 맞는 하드코딩된 응답
     */
    @Test
    void stubExample() {
        // Stub EmailService - 항상 true를 반환
        EmailService stubEmailService = new EmailService() {
            @Override
            public void send(String to, String subject, String body) {
                // 아무것도 하지 않음 - stub은 동작하지 않아도 됨
            }

            @Override
            public boolean isValidEmail(String email) {
                // 항상 true를 반환하는 stub
                return true;
            }
        };

        // Stub Repository - 미리 정의된 응답 반환
        UserRepository stubRepository = new UserRepository() {
            @Override
            public User save(User user) {
                // 항상 ID 123을 가진 사용자 반환
                return new User(123L, user.getName(), user.getEmail());
            }

            @Override
            public Optional<User> findById(Long id) {
                // 특정 ID에 대해서만 미리 준비된 사용자 반환
                if (id == 123L) {
                    return Optional.of(new User(123L, "Stub User", "stub@example.com"));
                }
                return Optional.empty();
            }

            @Override
            public List<User> findAll() {
                // 항상 2명의 사용자를 반환
                return Arrays.asList(
                    new User(1L, "User1", "user1@example.com"),
                    new User(2L, "User2", "user2@example.com")
                );
            }

            @Override
            public void deleteById(Long id) {
                // 아무것도 하지 않음
            }
        };

        UserService service = new UserService(stubRepository, stubEmailService);
        
        // Stub은 항상 동일한 결과 반환
        User user = service.createUser("Any Name", "any@email.com");
        assertThat(user.getId()).isEqualTo(123L);
        
        Optional<User> found = service.getUser(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Stub User");
    }

    /**
     * 3. SPY - 호출 정보를 기록하는 객체
     * 
     * 특징:
     * - 메서드 호출 횟수, 파라미터 등을 기록
     * - 나중에 검증 가능
     * - 실제 동작도 수행할 수 있음
     */
    @Test
    void spyExample() {
        // Spy EmailService - 호출 정보를 기록
        class SpyEmailService implements EmailService {
            private int sendCallCount = 0;
            private String lastTo;
            private String lastSubject;
            private String lastBody;
            private List<String> emailValidationCalls = new ArrayList<>();

            @Override
            public void send(String to, String subject, String body) {
                sendCallCount++;
                lastTo = to;
                lastSubject = subject;
                lastBody = body;
            }

            @Override
            public boolean isValidEmail(String email) {
                emailValidationCalls.add(email);
                return email != null && email.contains("@");
            }

            // Spy 검증 메서드들
            public int getSendCallCount() { return sendCallCount; }
            public String getLastTo() { return lastTo; }
            public String getLastSubject() { return lastSubject; }
            public List<String> getEmailValidationCalls() { return emailValidationCalls; }
        }

        SpyEmailService spyEmailService = new SpyEmailService();
        
        UserRepository stubRepository = new UserRepository() {
            @Override
            public User save(User user) {
                user.setId(1L);
                return user;
            }
            
            @Override
            public Optional<User> findById(Long id) { return Optional.empty(); }
            @Override
            public List<User> findAll() { return Collections.emptyList(); }
            @Override
            public void deleteById(Long id) { }
        };

        UserService service = new UserService(stubRepository, spyEmailService);
        
        // 서비스 호출
        service.createUser("John", "john@example.com");
        service.createUser("Jane", "jane@example.com");
        
        // Spy를 통한 검증
        assertThat(spyEmailService.getSendCallCount()).isEqualTo(2);
        assertThat(spyEmailService.getLastTo()).isEqualTo("jane@example.com");
        assertThat(spyEmailService.getLastSubject()).isEqualTo("Welcome");
        assertThat(spyEmailService.getEmailValidationCalls()).hasSize(2);
    }

    /**
     * 4. MOCK - 예상되는 호출과 응답을 미리 프로그래밍한 객체
     * 
     * 특징:
     * - 기대하는 동작을 미리 정의
     * - 호출 순서, 횟수, 파라미터 검증
     * - Mockito 같은 프레임워크 주로 사용
     */
    @Test
    void mockExample() {
        // Mockito를 사용한 Mock 객체 생성
        EmailService mockEmailService = mock(EmailService.class);
        UserRepository mockRepository = mock(UserRepository.class);
        
        // Mock 동작 정의 (기대값 설정)
        User userToSave = new User(null, "John", "john@example.com");
        User savedUser = new User(1L, "John", "john@example.com");
        
        when(mockRepository.save(any(User.class))).thenReturn(savedUser);
        when(mockEmailService.isValidEmail("john@example.com")).thenReturn(true);
        
        // UserService 생성 및 실행
        UserService service = new UserService(mockRepository, mockEmailService);
        User result = service.createUser("John", "john@example.com");
        
        // 결과 검증
        assertThat(result.getId()).isEqualTo(1L);
        
        // Mock 호출 검증
        verify(mockRepository, times(1)).save(any(User.class));
        verify(mockEmailService, times(1)).isValidEmail("john@example.com");
        verify(mockEmailService, times(1)).send(
            eq("john@example.com"), 
            eq("Welcome"), 
            eq("Welcome to our service!")
        );
        
        // 다른 메서드는 호출되지 않았음을 검증
        verify(mockRepository, never()).findById(any());
        verify(mockRepository, never()).deleteById(any());
    }

    /**
     * 5. FAKE - 실제 동작하지만 프로덕션에는 부적합한 단순화된 구현
     * 
     * 특징:
     * - 실제로 동작하는 구현체
     * - 메모리 기반 저장소 등 단순화된 버전
     * - 빠르고 예측 가능한 동작
     * - 여러분의 프로젝트의 Repository 구현체들이 바로 이것!
     */
    @Test
    void fakeExample() {
        // Fake Repository - 실제로 동작하는 메모리 기반 구현
        class FakeUserRepository implements UserRepository {
            private final Map<Long, User> storage = new ConcurrentHashMap<>();
            private Long idCounter = 1L;

            @Override
            public User save(User user) {
                if (user.getId() == null) {
                    user.setId(idCounter++);
                }
                storage.put(user.getId(), user);
                return user;
            }

            @Override
            public Optional<User> findById(Long id) {
                return Optional.ofNullable(storage.get(id));
            }

            @Override
            public List<User> findAll() {
                return new ArrayList<>(storage.values());
            }

            @Override
            public void deleteById(Long id) {
                storage.remove(id);
            }
        }

        // Fake EmailService - 실제로 동작하지만 이메일은 보내지 않음
        class FakeEmailService implements EmailService {
            private final List<EmailRecord> sentEmails = new ArrayList<>();
            
            static class EmailRecord {
                String to, subject, body;
                EmailRecord(String to, String subject, String body) {
                    this.to = to;
                    this.subject = subject;
                    this.body = body;
                }
            }

            @Override
            public void send(String to, String subject, String body) {
                sentEmails.add(new EmailRecord(to, subject, body));
            }

            @Override
            public boolean isValidEmail(String email) {
                return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
            }

            public List<EmailRecord> getSentEmails() {
                return sentEmails;
            }
        }

        FakeUserRepository fakeRepository = new FakeUserRepository();
        FakeEmailService fakeEmailService = new FakeEmailService();
        UserService service = new UserService(fakeRepository, fakeEmailService);
        
        // 실제로 동작하는 fake 구현체 테스트
        User user1 = service.createUser("John", "john@example.com");
        User user2 = service.createUser("Jane", "jane@example.com");
        
        assertThat(user1.getId()).isEqualTo(1L);
        assertThat(user2.getId()).isEqualTo(2L);
        
        // Fake는 실제로 데이터를 저장하고 조회 가능
        Optional<User> found = service.getUser(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John");
        
        // Fake email service도 실제로 기록을 유지
        assertThat(fakeEmailService.getSentEmails()).hasSize(2);
        
        // 실제로 동작하므로 복잡한 시나리오도 테스트 가능
        List<User> allUsers = fakeRepository.findAll();
        assertThat(allUsers).hasSize(2);
        
        fakeRepository.deleteById(1L);
        assertThat(fakeRepository.findAll()).hasSize(1);
    }

    /**
     * 언제 어떤 Test Double을 사용해야 할까?
     * 
     * 1. Dummy: 파라미터를 채워야 하지만 실제로 사용하지 않을 때
     * 2. Stub: 테스트에 필요한 간단한 응답만 필요할 때
     * 3. Spy: 호출 여부와 호출 방식을 검증해야 할 때
     * 4. Mock: 복잡한 상호작용과 호출 순서를 검증해야 할 때
     * 5. Fake: 실제 동작이 필요하지만 외부 의존성을 제거하고 싶을 때
     */
}