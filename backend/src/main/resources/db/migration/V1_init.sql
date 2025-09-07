-- Ensure UTF8MB4 (optional)
SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- USERS
CREATE TABLE IF NOT EXISTS users (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  email          VARCHAR(255) UNIQUE,                          -- 소셜 로그인 대비 NULL 허용
  password_hash  VARCHAR(255),                                 -- local만 사용
  nickname       VARCHAR(50)  NOT NULL UNIQUE,
  avatar_url     VARCHAR(512),
  role           ENUM('USER','ADMIN') DEFAULT 'USER',
  provider       VARCHAR(20) NOT NULL DEFAULT 'local',         -- local | kakao | google
  provider_id    VARCHAR(100),                                 -- 소셜 고유 ID
  refresh_token  VARCHAR(512),                                 -- (선택) 백업/감사용
  email_verified BOOLEAN DEFAULT FALSE,
  last_login_at  DATETIME DEFAULT CURRENT_TIMESTAMP,

  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_users_provider (provider, provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- POSTS
CREATE TABLE IF NOT EXISTS posts (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  slug           VARCHAR(180) NOT NULL UNIQUE,
  title          VARCHAR(200) NOT NULL,
  content        LONGTEXT     NOT NULL,
  thumbnail_url  VARCHAR(512),
  category       VARCHAR(60),
  status         ENUM('PUBLISHED','DRAFT') DEFAULT 'PUBLISHED',
  views          INT DEFAULT 0,

  author_id      BIGINT NOT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_posts_author FOREIGN KEY(author_id) REFERENCES users(id),
  INDEX idx_posts_author   (author_id),
  INDEX idx_posts_category (category),
  INDEX idx_posts_title    (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- TAGS
CREATE TABLE IF NOT EXISTS tags (
  id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- POST_TAGS (N:M)
CREATE TABLE IF NOT EXISTS post_tags (
  post_id BIGINT NOT NULL,
  tag_id  BIGINT NOT NULL,
  PRIMARY KEY (post_id, tag_id),
  CONSTRAINT fk_pt_post FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_pt_tag  FOREIGN KEY(tag_id)  REFERENCES tags(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- COMMENTS (트리형 구조 선택지: path + depth)
CREATE TABLE IF NOT EXISTS comments (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id    BIGINT NOT NULL,
  parent_id  BIGINT NULL,
  author_id  BIGINT NOT NULL,
  content    TEXT   NOT NULL,
  depth      INT    DEFAULT 0,
  path       VARCHAR(1000),             -- 예: /1/4/9
  is_deleted TINYINT(1) DEFAULT 0,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_comments_post_path (post_id, path),
  CONSTRAINT fk_comments_post   FOREIGN KEY(post_id)   REFERENCES posts(id)  ON DELETE CASCADE,
  CONSTRAINT fk_comments_author FOREIGN KEY(author_id) REFERENCES users(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
