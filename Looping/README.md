games
- game_id CHAR(11) PRIMARY
- white_player_id CHAR(11) FOREIGN
- black_player_id CHAR(11) FOREIGN
- winner_id CHAR(11) FOREIGN
- tournament_id CHAR(11) FOREIGN
- moves TEXT
- start_time DATETIME
- end_time DATETIME
- victory_type CHAR(9)

settings
- player_id CHAR(11) PRIMARY FOREIGN
- theme VARCHAR(255)
- show_legal_moves LOGICAL
- auto_promote_queen LOGICAL
- ai_difficulty_level INT

achievements
- achievement_id CHAR(11) PRIMARY
- name VARCHAR(255)
- description VARCHAR(255)

players
- player_id CHAR(11) PRIMARY
- handle VARCHAR(32) PRIMARY
- email VARCHAR(254) PRIMARY
- password_hash VARCHAR(64)
- display_name VARCHAR(1024)
- avatar VARCHAR(260)
- birthday DATE
- sex VARCHAR(32)
- rating INT
- location VARCHAR(128)
- joined_at DATETIME
- updated_at DATETIME
- last_login DATETIME
- is_verified LOGICAL
- is_banned LOGICAL

puzzles
- puzzle_id CHAR(11) PRIMARY
- moves TEXT
- solutions TEXT
- rating INT
- theme VARCHAR(255)
- created_at DATETIME

tournaments
- tournament_id CHAR(11) PRIMARY
- name VARCHAR(255)
- description VARCHAR(255)
- start_time DATETIME
- end_time DATETIME
- winner_id CHAR(11) FOREIGN