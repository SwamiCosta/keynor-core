-- Script:  S01 — Primordial Era initial content
-- Purpose: Seeds all universe data from aniannoth-overview/content/ into keynor-core.
--          Covers eras, maps, one character (Omnia) and one lore entry (The Last Prayer).
-- Target:  maps, eras, map_eras, characters, character_categories, character_tags,
--          universe_entity_images, lore, lore_categories, lore_tags
-- Effect:  4 new rows across parent tables, plus categories, tags, and image join rows.
--          All entities set to status CANON. Idempotent via ON CONFLICT DO NOTHING,
--          except the IMAGES section which is NOT idempotent (no unique constraint).
-- Author:  Siegmund (Level 2 — keynor-core)
-- Date:    2026-06-02
--
-- AUTHORIZATION REQUIRED before executing this script.
-- Run against the target database after Flyway migrations V1–V4 have been applied.
-- Command: psql -U <user> -d keynor_core -f S01__seed_primordial_era_content.sql

-- ============================================================
-- MAPS
-- ============================================================

INSERT INTO maps (id, name, map_type, image)
VALUES ('omniverse', 'The Omniverse', 'ABSTRACT', '')
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- ERAS
-- ============================================================

INSERT INTO eras (id, name, era_order, period, summary, map_type, default_map, color)
VALUES (
    'primordial',
    'The Primordial Era',
    0,
    'Before Creation',
    'The age before the material world came into being — a time of pure essence, divine forces, and the foundation upon which all existence would be built. No navigable world exists here; only the infinite.',
    'ABSTRACT',
    'omniverse',
    '#7c3aed'
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- MAP ↔ ERA ASSOCIATIONS
-- ============================================================

INSERT INTO map_eras (map_id, era_id)
VALUES ('omniverse', 'primordial')
ON CONFLICT (map_id, era_id) DO NOTHING;

-- ============================================================
-- CHARACTERS
-- ============================================================

INSERT INTO characters (id, name, summary, body, status, timeline_founded, timeline_destroyed, created_at, updated_at)
VALUES (
    'b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d',
    'Omnia',
    'The final word of all things, Omnia stands at the uttermost edge of becoming — goddess of totality, dwelling in a perfection no mortal thought has reached. She is not worshipped as one among many. She is acknowledged as the bound no eye can see past, the point at which existence stops and the unimaginable begins.',
    E'## Omnia\n\nShe is the last answer and the question that precedes all others. Omnia stands where existence ends and the unimaginable begins — a realm no thought has charted, no tongue has named, and no creature of flesh or spirit has returned from having witnessed. She is not worshipped as one deity among many. She is recognized, quietly, as the ceiling of all things: the pinnacle at which evolution stills, the point of absolute perfection from which there is nothing further to become.\n\nNothing exists beyond Omnia. This is not a statement of faith. It is, her faithful say, the only honest description of what *everything* means.\n\nHer true nature belongs to no theology, for no theology reaches that far. Her origins are unknown. Her will, if she possesses one, is incomprehensible. Her motivations have never been recorded, because no consciousness capable of comprehending them has ever existed. The priests who speak of her speak always of the distance between themselves and any real understanding of what she is.\n\n### The Representation\n\nWhat mortals know of Omnia, they know through her representation — an image their minds could hold without breaking. It is taught, in every tradition that honors her, that this image is one facet among infinite facets: a single face turned just enough toward the comprehensible to be perceived. It is not Omnia. It is the part of Omnia that can be looked at.\n\nShe appears as a feminine figure of immeasurable stillness. In one hand she carries the Scepter of Infinity; in the other, a twelve-sided polyhedron — a solid that in its very geometry gestures at the boundless, since twelve, the priests quietly agree, is merely the number their language settled upon for what is truly beyond counting.\n\nHer crown is not worn. It is *fixed* — indistinguishable from the face beneath it, as though it always was and never was placed. Her skin and her hair carry every color the eye can perceive, each shifting across the full spectrum as light moves, so that she appears at once like all things and like nothing that can be simply named.\n\n### The Gaze\n\nThere is a teaching that follows every depiction of Omnia: she is always looking at you.\n\nNot facing you — *looking at you*. Directly. Unflinchingly. It does not matter the angle from which you approach. No profile exists to catch. No side to slip past. Every faithful account agrees: she faces forward, and forward is always where you are.\n\nThis is why it is said she possesses twelve faces — one for every direction, extending to all sides, making it impossible to stand anywhere that is not in front of her. The number twelve is an approximation. The truth is that her faces are infinite, and so her gaze is infinite, and so there is no place in all of existence that has ever looked away from her.\n\nHer gaze is not a threat. It is a fact about the structure of things. To exist is to be seen by Omnia. To be seen by Omnia is to be, in some irreducible way, held within the totality she embodies.\n\nThere is no escaping it. Most who carry this understanding do not find it frightening. They find it final — and in finality, inexplicably, they find rest.',
    'CANON',
    'primordial',
    NULL,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO character_categories (character_id, category)
VALUES ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'DEITY')
ON CONFLICT (character_id, category) DO NOTHING;

INSERT INTO character_tags (character_id, tag)
VALUES
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'deity'),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'goddess'),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'divinity'),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'totality'),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'infinite'),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'primordial')
ON CONFLICT (character_id, tag) DO NOTHING;

-- ============================================================
-- IMAGES
-- ============================================================

-- NOTE: universe_entity_images has no unique constraint.
-- This INSERT is NOT idempotent — run it only once against each target database.

INSERT INTO universe_entity_images (entity_id, image_url, display_order)
VALUES
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-1.png', 0),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-2.png', 1),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-3.png', 2),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-4.png', 3),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-5.png', 4),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-6.png', 5),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-7.png', 6),
    ('b3e2c1d4-f5e6-4a7b-8c9d-0e1f2a3b4c5d', 'https://pub-f1c218252a1647b7a5079e610730dc44.r2.dev/characters/Omnia-8.png', 7);

-- ============================================================
-- LORE
-- ============================================================

INSERT INTO lore (id, name, summary, body, status, timeline_founded, timeline_destroyed, created_at, updated_at)
VALUES (
    'a1b2c3d4-e5f6-4789-8abc-def012345678',
    'The Last Prayer',
    'A teaching preserved across generations of the Omnia faith — of those who sought communion with the goddess and were never seen again, and the parable told in the temples to explain why.',
    E'### The Parable of the Old Tree\n\nThere is a story told in the temples — old enough that no one remembers its source.\n\nA boy lived across from an old tree. He was a good climber, light and quick, and the tree tempted him. His father took him aside one evening and told him plainly: *never climb that tree. The branches are old. They will break under you.* The boy heard his father. He also heard the tree.\n\nHe climbed it anyway — a dozen times, two dozen, throughout the years of his childhood. The branches held. They always held. And so, in his mind, the warning aged into foolishness. His father had simply been wrong.\n\nDecades passed. The boy became a man. He had a son of his own, and one afternoon he told the story — not as a cautionary tale, but as evidence. *My father warned me,* he said, *but I knew better, and nothing ever happened.* He wanted to prove it. He crossed the yard and climbed the tree again, the way he had climbed it a hundred times before, and settled his weight onto one of the lower branches.\n\nThe branch did not consider his history. It broke. He fell.\n\nThe priests tell this story not to speak of trees, but of Omnia.\n\n### The Last Prayer\n\nIt is known — recorded across generations in the quieter annals of the faith — that monks and priests who attempt to pray in direct communion with Omnia do not simply die.\n\nThey disappear. Completely. No body is recovered. No trace persists. Not even the particular quality of their absence lingers, the way a room sometimes holds the shape of someone who has left it. After the prayer, there is nothing where they were. It is as though they never occupied that space at all.\n\nThis does not happen to everyone who prays. The faith is careful on this point. It happens only to those who have climbed high enough — the most spiritually refined, the most genuinely devout, those whose practice has made them transparent enough to actually evoke the presence of the goddess rather than merely speak in her direction. The ordinary worshipper is safe in their distance. It is only those who draw close enough to be truly seen who vanish.\n\nEvery generation produces at least one who believes they will be different. They have climbed the tree a hundred times. The branches always held. They are ready, they are pure, they are certain. They make the prayer.\n\nNone of them return.\n\nOthers have been taken by simpler things — curiosity, mostly, or the particular pride of the scholarly. They had no great devotion. They simply wanted to know what was on the other side. They received their answer in full, and were not there afterward to share it.\n\nWhat awaits beyond the prayer, no living person can say with authority. There are three accounts preserved across the traditions.\n\nThe first holds that what is called vanishing is not death at all, but the eternal and ecstatic embrace of the goddess — that those who disappear are not destroyed but *received*, gathered entirely into the totality of Omnia, which is to say, into everything. In this reading, the prayer is not an ending but a completion, and those who have made it are the only ones who have ever truly arrived anywhere.\n\nThe second holds that the dissolution of the one who prays is a punishment — a correction, swift and absolute, for the insolence of imagining oneself close enough to Omnia to address her directly. That she is not deaf to these prayers. That she hears them precisely. And that the silence that follows is her only reply.\n\nThe third account offers no moral and asks for none: perhaps it is simply what happens when something finite touches something infinite. Perhaps there is no intent behind it at all — no embrace, no punishment, only consequence. A moth does not offend the flame.\n\nNo one who has made the leap of faith has returned to tell us which of these is true. This, perhaps, is the most honest thing about Omnia: the question she raises most clearly is the one she will never allow to be answered.',
    'CANON',
    'primordial',
    NULL,
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO lore_categories (lore_id, category)
VALUES ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'MYTH')
ON CONFLICT (lore_id, category) DO NOTHING;

INSERT INTO lore_tags (lore_id, tag)
VALUES
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'omnia'),
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'faith'),
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'cult'),
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'ritual'),
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'warning'),
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'prayer'),
    ('a1b2c3d4-e5f6-4789-8abc-def012345678', 'primordial')
ON CONFLICT (lore_id, tag) DO NOTHING;
