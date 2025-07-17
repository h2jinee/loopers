package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
public class UserEntity extends BaseEntity {

	@Id
	private String userId;

	private String name;

	private String email;

	private String birth;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender")
	private Gender gender;

	public enum Gender {
		M, F
	}

	public UserEntity(
		String userId,
		String name,
		Gender gender,
		String birth,
		String email

	) {
		UserValidator.validateUserId(userId);
		UserValidator.validateBirth(birth);
		UserValidator.validateEmail(email);

		this.userId = userId;
		this.name = name;
		this.gender = gender;
		this.birth = birth;
		this.email = email;

	}
}
