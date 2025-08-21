package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Column(unique = true, nullable = false)
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

	public User(
		String userId,
		String name,
		Gender gender,
		String birth,
		String email

	) {
		this.userId = userId;
		this.name = name;
		this.gender = gender;
		this.birth = birth;
		this.email = email;

	}
}
