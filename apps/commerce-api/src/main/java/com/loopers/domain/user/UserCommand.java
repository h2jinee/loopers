package com.loopers.domain.user;

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.domain.user.vo.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class UserCommand {

    public record Create(
        UserId userId,
        String name,
        UserEntity.Gender gender,
        Birth birth,
        Email email
    ) {
        public Create {
            if (name == null || name.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "이름은 필수입니다.");
            }
			if (gender == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 값입니다.");
            }
		}
    }
    
    public record GetOne(
        String userId
    ) {
        public GetOne {
            if (userId == null || userId.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
            }
        }
    }
}
