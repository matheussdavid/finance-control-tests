package br.com.financecontrol.builders;

import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.data.UserFaker;

/**
 * Builder fluent de usuário de teste. Por padrão gera dados aleatórios e
 * únicos (garante testes independentes); permite sobrescrever cada campo.
 */
public final class TestUserBuilder {

    private String name = UserFaker.fullName();
    private String username = UserFaker.username();
    private String email = UserFaker.email();
    private String password = UserFaker.password();

    private TestUserBuilder() {
    }

    public static TestUserBuilder aUser() {
        return new TestUserBuilder();
    }

    public TestUserBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TestUserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public TestUserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public TestUserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public TestUser build() {
        return TestUser.of(name, username, email, password);
    }
}