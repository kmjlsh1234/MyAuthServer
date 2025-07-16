CREATE TABLE `users`
(
    `user_id`    bigint      NOT NULL AUTO_INCREMENT COMMENT '유저고유번호',
    `login_id`   varchar(100)         DEFAULT NULL COMMENT '로그인아이디',
    `password`   varchar(60)          DEFAULT NULL COMMENT '패스워드',
    `mobile`     varchar(12) NOT NULL COMMENT '휴대폰번호',
    `email`      varchar(100)         DEFAULT NULL COMMENT '이메일',
    `login_type` tinyint     NOT NULL DEFAULT '0' COMMENT '로그인 타입 id_pass:0, social:1, guest:2',
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성시각',
    `updated_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '변경시각',
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='유저';

CREATE TABLE `jwt_record`
(
    `issue_no`         bigint    NOT NULL AUTO_INCREMENT COMMENT '발급번호',
    `user_id`          bigint    NOT NULL COMMENT '고유번호',
    `refresh_token_id` bigint    NOT NULL COMMENT '리프레쉬토큰고유번호',
    `ip_address`       varchar(20)        DEFAULT NULL COMMENT '아이피주소',
    `expire_datetime`  datetime  NOT NULL COMMENT '만료일시',
    `logout_at`        timestamp NULL     DEFAULT NULL COMMENT '로그아웃시각',
    `created_at`       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성시각',
    PRIMARY KEY (`issue_no`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='jwt토큰발급기록';

CREATE TABLE `refresh_token`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '리프레쉬토큰 고유번호',
    `user_id`         bigint       NOT NULL COMMENT '유저고유번호',
    `refresh_token`   varchar(100) NOT NULL COMMENT '리프레쉬토큰',
    `expire_datetime` datetime     NOT NULL COMMENT '만료일시',
    `created_at`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성시각',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='리프레시 토큰';