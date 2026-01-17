CREATE TABLE achievements (
	achievement_id CHAR(11) NOT NULL,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255) DEFAULT NULL
);
CREATE TABLE players (
	player_id CHAR(11) NOT NULL,
	handle VARCHAR(32) NOT NULL,
	email VARCHAR(254) NOT NULL,
	password_hash VARCHAR(64) NOT NULL,
	display_name VARCHAR(1024) DEFAULT NULL,
	avatar VARCHAR(260) DEFAULT NULL,
	birthday DATE DEFAULT NULL,
	sex VARCHAR(32) DEFAULT NULL,
	rating INT(11) DEFAULT 1200,
	location VARCHAR(128) DEFAULT NULL,
	joined_at DATETIME DEFAULT CURRENT_TIMESTAMP(),
	updated_at DATETIME DEFAULT NULL,
	last_login DATETIME DEFAULT NULL,
	is_verified TINYINT(1) DEFAULT 0,
	is_banned TINYINT(1) DEFAULT 0
);
CREATE TABLE puzzles (
	puzzle_id CHAR(11) NOT NULL,
	moves TEXT NOT NULL,
	solutions TEXT NOT NULL,
	rating INT(11) DEFAULT NULL,
	theme VARCHAR(255) DEFAULT NULL,
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP()
);
CREATE TABLE settings (
	player_id CHAR(11) NOT NULL,
	theme VARCHAR(255) DEFAULT 'default',
	show_legal_moves TINYINT(1) DEFAULT 1,
	auto_promote_queen TINYINT(1) DEFAULT 0,
	ai_difficulty_level INT(11) DEFAULT 1
);
CREATE TABLE tournaments (
	tournament_id CHAR(11) NOT NULL,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255) DEFAULT NULL,
	start_time DATETIME DEFAULT NULL,
	end_time DATETIME DEFAULT NULL,
	winner_id CHAR(11) DEFAULT NULL
);
CREATE TABLE unlocks (
	player_id CHAR(11) NOT NULL,
	achievement_id CHAR(11) NOT NULL,
	unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE games (
	game_id CHAR(11) NOT NULL,
	white_player_id CHAR(11) NOT NULL,
	black_player_id CHAR(11) NOT NULL,
	winner_id CHAR(11) DEFAULT NULL,
	tournament_id CHAR(11) DEFAULT NULL,
	moves TEXT DEFAULT NULL,
	start_time DATETIME DEFAULT NULL,
	end_time DATETIME DEFAULT NULL,
	victory_type CHAR(9) DEFAULT NULL
);
CREATE TABLE participants (
	tournament_id CHAR(11) NOT NULL,
	player_id CHAR(11) NOT NULL,
	joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE
	achievements
ADD
	CONSTRAINT pk_achievements PRIMARY KEY (achievement_id);
ALTER TABLE
	players
ADD
	CONSTRAINT pk_players PRIMARY KEY (player_id),
ADD
	CONSTRAINT uq_players_handle UNIQUE (handle),
ADD
	CONSTRAINT uq_players_email UNIQUE (email);
ALTER TABLE
	puzzles
ADD
	CONSTRAINT pk_puzzles PRIMARY KEY (puzzle_id);
ALTER TABLE
	settings
ADD
	CONSTRAINT pk_settings PRIMARY KEY (player_id),
ADD
	CONSTRAINT fk_settings_player FOREIGN KEY (player_id) REFERENCES players (player_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE
	tournaments
ADD
	CONSTRAINT pk_tournaments PRIMARY KEY (tournament_id),
ADD
	CONSTRAINT fk_tournaments_winner FOREIGN KEY (winner_id) REFERENCES players (player_id) ON DELETE
SET
	NULL ON UPDATE CASCADE;
ALTER TABLE
	unlocks
ADD
	CONSTRAINT pk_unlocks PRIMARY KEY (player_id, achievement_id),
ADD
	CONSTRAINT fk_unlocks_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE,
ADD
	CONSTRAINT fk_unlocks_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(achievement_id) ON DELETE CASCADE;
ALTER TABLE
	games
ADD
	CONSTRAINT pk_games PRIMARY KEY (game_id),
ADD
	CONSTRAINT fk_games_white FOREIGN KEY (white_player_id) REFERENCES players (player_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD
	CONSTRAINT fk_games_black FOREIGN KEY (black_player_id) REFERENCES players (player_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD
	CONSTRAINT fk_games_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (tournament_id) ON DELETE
SET
	NULL ON UPDATE CASCADE,
ADD
	CONSTRAINT fk_games_winner FOREIGN KEY (winner_id) REFERENCES players (player_id) ON DELETE
SET
	NULL ON UPDATE CASCADE;
ALTER TABLE
	participants
ADD
	CONSTRAINT pk_participants PRIMARY KEY (tournament_id, player_id),
ADD
	CONSTRAINT fk_participants_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id) ON DELETE CASCADE,
ADD
	CONSTRAINT fk_participants_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE;