INSERT INTO
	players (
		player_id,
		handle,
		email,
		password_hash,
		display_name,
		avatar,
		birthday,
		sex,
		rating,
		location,
		joined_at,
		updated_at,
		last_login,
		is_verified,
		is_banned
	)
VALUES
	(
		'00000000001',
		'test',
		'test@example.org',
		'$2a$10$9qJDPUYa95RtX/BliLF9Nepu6eB99pqa66YV5RuBx9rOcL3A5BpHq',
		NULL,
		NULL,
		NULL,
		NULL,
		'1200',
		NULL,
		'2026-01-01 00:00:00',
		NULL,
		NULL,
		'0',
		'0'
	),
	(
		'00000000002',
		'test2',
		'test2@example.org',
		'$2a$10$9qJDPUYa95RtX/BliLF9Nepu6eB99pqa66YV5RuBx9rOcL3A5BpHq',
		NULL,
		NULL,
		NULL,
		NULL,
		'1200',
		NULL,
		'2026-01-01 00:00:00',
		NULL,
		NULL,
		'0',
		'0'
	);
INSERT INTO
	`tournaments` (
		`tournament_id`,
		`name`,
		`description`,
		`start_time`,
		`end_time`,
		`winner_id`
	)
VALUES
	(
		'TED',
		'Tournoi à élimination directe',
		'Dans ce tournoi, aucune pitié, soit vous réussissez à gagner soit vous êtes éléminer, si vous parvenez à faire 6 victoires successivement , vous gagnez',
		'2025-12-15 14:00:00',
		'2025-12-15 18:00:00',
		NULL
	),
	(
		'T_NOOB',
		'Tournoi des Débutants',
		'Pour apprendre à jouer aux echecs dans un environnement stressant',
		'2026-02-01 10:00:00',
		NULL,
		NULL
	),
	(
		'T_PRO',
		'HexaChess Pro League',
		'Réservez aux meilleurs joueurs Hexachess (1000 elo minimum ) ',
		'2026-05-20 20:00:00',
		NULL,
		NULL
	);