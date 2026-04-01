import re

old_sql_path = r"d:\Minh\FU_Learning\EXE201\PixelMage_Rebase\project_src\BE\PixelMageEcomerceProject\src\main\resources\Data-init.sql"
new_sql_path = r"d:\Minh\FU_Learning\EXE201\PixelMage_Rebase\project_src\BE\PixelMageEcomerceProject\src\main\resources\Data-init.tmp.sql"

legendary_ids = {1, 2, 7, 11, 14, 17, 21, 22}
rare_ids = {3, 4, 5, 6, 8, 9, 10, 12, 13, 15, 16, 18, 19, 20, 33, 34, 35, 36, 47, 48, 49, 50, 61, 62, 63, 64, 75, 76, 77, 78}

def get_rarity(card_id):
    if card_id in legendary_ids:
        return 'LEGENDARY'
    elif card_id in rare_ids:
        return 'RARE'
    else:
        return 'COMMON'

# We have new chunks from the planning document, we will build the final SQL
sql = """BEGIN;

-- ================================================================
-- SECTION 1: MASTER DATA (no FK dependencies)
-- ================================================================
INSERT INTO Roles (role_id, role_name) VALUES
(1, 'ADMIN'), (2, 'STAFF'), (3, 'USER')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO card_frameworks (framework_id, name, description, is_active, created_at, updated_at) VALUES
(1, 'Rider-Waite-Smith', 'Bộ Tarot chuẩn 78 lá, phát triển năm 1909 bởi Arthur Edward Waite và Pamela Colman Smith. Nền tảng hình ảnh cho toàn bộ card_templates của PixelMage.', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (framework_id) DO NOTHING;

INSERT INTO spreads (name, description, position_count, min_cards_required) VALUES
('Một Lá (1-Card Draw)', 'Câu hỏi đơn giản, thông điệp ngày, định hướng nhanh', 1, 1),
('Ba Lá (3-Card Spread)', 'Quá Khứ - Hiện Tại - Tương Lai', 3, 3),
('Celtic Cross (Thập Tự Celt)', 'Trải bài 10 lá cung cấp một nhìn toàn cảnh về mọi khía cạnh của vấn đề.', 10, 10),
('Tình Yêu (Relationship Spread)', 'Đánh giá cấu trúc, ưu nhược điểm và tương lai của một mối quan hệ.', 7, 7)
ON CONFLICT (name) DO NOTHING;

INSERT INTO achievements (name, description, condition_type, condition_value, pm_point_reward, is_hidden) VALUES
('Collector Novice',  'Sở hữu 5 thẻ bài liên kết',    'CARD_COUNT',         5,  50,  false),
('Card Master',       'Sở hữu 20 thẻ bài liên kết',   'CARD_COUNT',        20, 200, false),
('Secret Archivist',  '???',                            'COLLECTION_COMPLETE', 1, 500,  true),
('First Legend',      'Sở hữu 1 lá LEGENDARY',         'RARITY_COUNT',       1, 100, false)
ON CONFLICT DO NOTHING;

INSERT INTO PROMOTIONS (name, description, discount_type, discount_value, start_date, end_date, created_at, updated_at) VALUES
('Grand Opening Sale', 'Khuyến mãi khai trương PixelMage', 'PERCENTAGE', 15.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ================================================================
-- SECTION 2: ACCOUNTS + CORE CATALOG
-- ================================================================
INSERT INTO Accounts (customer_id, email, password, name, auth_provider, is_active, email_verified, avatar_url, Role_id, created_at, updated_at) VALUES
(1, 'admin@pixelmage.com',  '$2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG', 'System Admin',  'LOCAL', true, true, 'https://api.dicebear.com/7.x/bottts/svg?seed=admin',  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'staff@pixelmage.com',  '$2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG', 'Staff Member',  'LOCAL', true, true, 'https://api.dicebear.com/7.x/bottts/svg?seed=staff',  2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'user@pixelmage.com',   '$2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG', 'Test User',     'LOCAL', true, true, 'https://api.dicebear.com/7.x/bottts/svg?seed=testuser', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (customer_id) DO NOTHING;

"""

with open(old_sql_path, "r", encoding="utf-8") as f:
    content = f.read()

# Extract card_templates
card_tmpl_match = re.search(r"INSERT INTO card_templates(.*?)(ON CONFLICT|INSERT INTO)", content, re.DOTALL)
if card_tmpl_match:
    card_v = card_tmpl_match.group(1).strip().rstrip(';')

    # We need to add framework_id=1 to each and update rarity
    # Format of insert: (id, name, desc, design, arcana, suit, cardnum, rarity, active, image, creat, upd)
    # We will just replace rarity and insert framework_id before is_active. Wait, schema in old:
    # (card_template_id, name, description, design_path, arcana_type, suit, card_number, rarity, is_active, image_path, created_at, updated_at)
    # We should write exact columns:
    
    lines = card_v.split('\n')
    new_lines = []
    
    # Let's cleanly parse it
    for line in lines:
        if '(' in line:
            m = re.match(r'\s*\((\d+),(.*?), (true|false), (.*)$', line)
            
            # Simple replace:
            cid_str = line.split(',')[0].strip(' (')
            try:
                cid = int(cid_str)
                rarity = get_rarity(cid)
                # replace rarity
                # existing rarity is the 8th value (1: id, 2: name, 3: desc, 4: design, 5: arcana, 6: suit, 7: num, 8: rarity)
                parts = line.split("', ")
                if len(parts) >= 6:
                    # actually regex is easier
                    # replace the rarity 'LEGENDARY'/'RARE'/'COMMON'
                    nl = re.sub(r"'LEGENDARY'|'RARE'|'COMMON'", f"'{rarity}'", line)
                    
                    # Also replace `/assets/cards/major/00.webp` with cloudinary
                    nl = nl.replace("'/assets/cards/", "'https://res.cloudinary.com/dscmqp5k5/image/upload/v1/cards/")
                    
                    # add framework_id
                    # old: rarity, is_active -> new: rarity, framework_id, is_active
                    nl = re.sub(r"('{rarity}'),\s*(true|false)", r"\1, 1, \2", nl)

                    new_lines.append(nl)
                else:
                    new_lines.append(line)
            except:
                pass


    sql += "INSERT INTO card_templates (card_template_id, name, description, design_path, arcana_type, suit, card_number, rarity, framework_id, is_active, image_path, created_at, updated_at) VALUES\n"
    sql += "\n".join(new_lines) + "\nON CONFLICT (card_template_id) DO NOTHING;\n\n"

# Extract divine_helpers
divine_match = re.search(r"INSERT INTO divine_helpers(.*?)(ON CONFLICT|INSERT INTO)", content, re.DOTALL)
if divine_match:
    sql += "INSERT INTO divine_helpers" + divine_match.group(1).strip().rstrip(';')
    sql += "\nON CONFLICT (divine_helper_id) DO NOTHING;\n\n"

sql += """-- ================================================================
-- SECTION 3: PRODUCTS + INVENTORY
-- ================================================================
INSERT INTO PRODUCTS (product_id, name, description, price, image_url, created_at, updated_at) VALUES
(1, 'PixelMage Standard Pack', '5 lá ngẫu nhiên. Cam kết ít nhất 1 RARE ở Slot 4. Tỷ lệ LEGENDARY Slot 5: 15%.', 79000.00, 'https://res.cloudinary.com/dscmqp5k5/image/upload/v1/packs/standard-pack.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'PixelMage Blister Promo', '3 Standard Packs + 1 lá Promo RARE đặc biệt.', 199000.00, 'https://res.cloudinary.com/dscmqp5k5/image/upload/v1/packs/blister-promo.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Major Sealed Box', '10 Standard Packs (50 lá). Đảm bảo ít nhất 1 lá LEGENDARY trong hộp.', 699000.00, 'https://res.cloudinary.com/dscmqp5k5/image/upload/v1/packs/major-sealed-box.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (product_id) DO NOTHING;

INSERT INTO INVENTORY (product_id, quantity, last_checked, created_at, updated_at) VALUES
(1, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 40,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 15,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_card_pools (product_id, card_template_id) SELECT 1, generate_series(1, 78) ON CONFLICT DO NOTHING;
INSERT INTO product_card_pools (product_id, card_template_id) SELECT 2, generate_series(1, 78) ON CONFLICT DO NOTHING;
INSERT INTO product_card_pools (product_id, card_template_id) SELECT 3, generate_series(1, 22) ON CONFLICT DO NOTHING;

-- ================================================================
-- SECTION 4: PHYSICAL CARDS + PACKS
-- ================================================================
INSERT INTO CARDS (card_id, card_template_id, product_id, status, serial_number, card_condition, software_uuid, created_at, updated_at) VALUES
(1, 1,  1, 'PENDING_BIND', 'PM-2026-001', 'NEW', 'db40a9f5-47e1-45bd-a37a-4c2cbfbdedbc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2,  1, 'PENDING_BIND', 'PM-2026-002', 'NEW', '089855fb-cc87-4aa4-8fc7-6a98daaf218b', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3,  1, 'PENDING_BIND', 'PM-2026-003', 'NEW', '2e3792cb-0ec5-4927-94a8-6acbfeadcecd', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CARDS (card_id, card_template_id, product_id, status, serial_number, card_condition, software_uuid, created_at, updated_at) VALUES
(4, 7,  1, 'READY', 'PM-2026-004', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(5, 23, 1, 'READY', 'PM-2026-005', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(6, 24, 1, 'READY', 'PM-2026-006', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 33, 1, 'READY', 'PM-2026-007', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(8, 34, 1, 'READY', 'PM-2026-008', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(9, 37, 1, 'READY', 'PM-2026-009', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 38, 1, 'READY', 'PM-2026-010', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 47, 1, 'READY', 'PM-2026-011', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(12, 51, 1, 'READY', 'PM-2026-012', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 52, 1, 'READY', 'PM-2026-013', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 61, 1, 'READY', 'PM-2026-014', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(15, 65, 1, 'READY', 'PM-2026-015', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 14, 1, 'READY', 'PM-2026-016', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(17, 11, 1, 'READY', 'PM-2026-017', 'NEW', gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO CARDS (card_id, card_template_id, product_id, status, serial_number, nfc_uid, software_uuid, owner_account_id, card_condition, linked_at, created_at, updated_at) VALUES
(18, 1,  1, 'LINKED', 'PM-2026-018', 'NFC-UID-A1B2C3', '67aaae91-3d7f-4f7f-aa00-e14b03f0b2da', 3, 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(19, 23, 1, 'LINKED', 'PM-2026-019', 'NFC-UID-D4E5F6', '140e69b5-68ff-45b6-b8c7-2e1d7bd2b5b3', 3, 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), 
(20, 33, 1, 'LINKED', 'PM-2026-020', 'NFC-UID-G7H8I9', '6ac1356e-827d-4b8c-bef1-43285cf61ee5', 3, 'NEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO PACKS (pack_id, product_id, status, created_by_account_id, created_at, version) VALUES
(1, 1, 'STOCKED', 2, CURRENT_TIMESTAMP, 0),
(2, 1, 'STOCKED', 2, CURRENT_TIMESTAMP, 0),
(3, 2, 'STOCKED', 2, CURRENT_TIMESTAMP, 0),
(4, 3, 'STOCKED', 2, CURRENT_TIMESTAMP, 0),
(5, 3, 'STOCKED', 2, CURRENT_TIMESTAMP, 0)
ON CONFLICT (pack_id) DO NOTHING;

INSERT INTO PACK_DETAILS (pack_id, card_id, position_index) VALUES (1, 4, 1),(1, 5, 2),(1, 6, 3),(1, 7, 4),(1, 8, 5);
INSERT INTO PACK_DETAILS (pack_id, card_id, position_index) VALUES (2, 9, 1),(2, 10, 2),(2, 11, 3),(2, 12, 4),(2, 13, 5);
INSERT INTO PACK_DETAILS (pack_id, card_id, position_index) VALUES (3, 14, 1),(3, 15, 2),(3, 16, 3),(3, 17, 4);
INSERT INTO PACK_DETAILS (pack_id, card_id, position_index) VALUES (4, 4, 1),(4, 8, 2);
INSERT INTO PACK_DETAILS (pack_id, card_id, position_index) VALUES (5, 7, 1),(5, 13, 2);

-- ================================================================
-- SECTION 5: USER ECONOMY
-- ================================================================
INSERT INTO pm_point_wallets (user_id, balance) VALUES
(1, 0), (2, 0), (3, 5000)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO vouchers (code, discount_pct, max_discount_vnd, owner_id, expires_at, is_used) VALUES
('PM-TEST-ABCD-1234', 10, 20000, 3, '2026-10-01 23:59:59', false),
('PM-TEST-USED-5678', 10, 20000, 3, '2026-10-01 23:59:59', true)
ON CONFLICT DO NOTHING;

-- ================================================================
-- SECTION 6: COLLECTIONS + PROGRESS
-- ================================================================
INSERT INTO COLLECTIONS (collection_id, collection_name, description, customer_id, is_public, is_active, collection_type, is_visible, reward_type, reward_data, created_by_admin_id, source, created_at, updated_at) VALUES
(1, 'Major Arcana Trio', 'Sưu tập 3 lá Major Arcana đầu tiên để test CollectionProgress.', 1, true, true, 'STANDARD', false, 'POINTS', '{"points":100}', 1, 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Wands Starter Set', 'Bộ sưu tập Wands cho Test User.', 3, false, true, 'STANDARD', true, null, null, null, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (collection_id) DO NOTHING;

INSERT INTO COLLECTION_ITEMS (collection_item_id, collection_id, card_template_id, required_quantity, added_at) VALUES
(1, 1, 1,  1, CURRENT_TIMESTAMP),
(2, 1, 2,  1, CURRENT_TIMESTAMP),
(3, 1, 3,  1, CURRENT_TIMESTAMP),
(4, 2, 23, 1, CURRENT_TIMESTAMP),
(5, 2, 33, 1, CURRENT_TIMESTAMP),
(6, 2, 34, 1, CURRENT_TIMESTAMP)
ON CONFLICT (collection_item_id) DO NOTHING;

INSERT INTO SET_STORIES (story_id, title, content, required_template_ids, cover_image_path, is_active, created_at, updated_at) VALUES
(1, 'Zero Arcana Saga', 'Khi người chơi sở hữu đủ 3 lá: The Fool, The Magician, The High Priestess, họ mở khóa câu chuyện khởi nguồn của hành trình Aion Zero.', '[1,2,3]', 'https://res.cloudinary.com/dscmqp5k5/image/upload/v1/stories/zero-arcana.webp', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Foundations of the Court', 'Mở khóa khi sở hữu đủ 4 lá Court of Wands.', '[33,34,35,36]', 'https://res.cloudinary.com/dscmqp5k5/image/upload/v1/stories/court-of-wands.webp', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (story_id) DO NOTHING;

INSERT INTO user_inventory (user_id, card_template_id, quantity, version, updated_at) VALUES
(3, 1,  1, 0, CURRENT_TIMESTAMP),
(3, 23, 1, 0, CURRENT_TIMESTAMP),
(3, 33, 1, 0, CURRENT_TIMESTAMP)
ON CONFLICT (user_id, card_template_id) DO UPDATE SET quantity = user_inventory.quantity + 1;

INSERT INTO user_achievements (user_id, achievement_id, granted_at, is_active) VALUES
(3, 4, CURRENT_TIMESTAMP, true)
ON CONFLICT (user_id, achievement_id) DO NOTHING;

INSERT INTO user_collection_progress (user_id, collection_id, owned_count, required_count, completion_percent, is_completed, last_updated_at) VALUES
(3, 2, 2, 3, 66.67, false, CURRENT_TIMESTAMP)
ON CONFLICT (user_id, collection_id) DO NOTHING;

-- ================================================================
-- SECTION 7: SEQUENCE RESET
-- ================================================================
SELECT setval(pg_get_serial_sequence('roles',                    'role_id'),           COALESCE((SELECT MAX(role_id)              FROM roles),                   0)+1, false);
SELECT setval(pg_get_serial_sequence('accounts',                 'customer_id'),        COALESCE((SELECT MAX(customer_id)          FROM accounts),                0)+1, false);
SELECT setval(pg_get_serial_sequence('card_frameworks',          'framework_id'),       COALESCE((SELECT MAX(framework_id)         FROM card_frameworks),         0)+1, false);
SELECT setval(pg_get_serial_sequence('card_templates',           'card_template_id'),   COALESCE((SELECT MAX(card_template_id)     FROM card_templates),          0)+1, false);
SELECT setval(pg_get_serial_sequence('divine_helpers',           'divine_helper_id'),   COALESCE((SELECT MAX(divine_helper_id)     FROM divine_helpers),          0)+1, false);
SELECT setval(pg_get_serial_sequence('spreads',                  'spread_id'),          COALESCE((SELECT MAX(spread_id)            FROM spreads),                 0)+1, false);
SELECT setval(pg_get_serial_sequence('products',                 'product_id'),         COALESCE((SELECT MAX(product_id)           FROM products),                0)+1, false);
SELECT setval(pg_get_serial_sequence('inventory',                'inventory_id'),       COALESCE((SELECT MAX(inventory_id)         FROM inventory),               0)+1, false);
SELECT setval(pg_get_serial_sequence('cards',                    'card_id'),            COALESCE((SELECT MAX(card_id)              FROM cards),                   0)+1, false);
SELECT setval(pg_get_serial_sequence('packs',                    'pack_id'),            COALESCE((SELECT MAX(pack_id)              FROM packs),                   0)+1, false);
SELECT setval(pg_get_serial_sequence('pack_details',             'pack_detail_id'),     COALESCE((SELECT MAX(pack_detail_id)       FROM pack_details),            0)+1, false);
SELECT setval(pg_get_serial_sequence('collections',              'collection_id'),      COALESCE((SELECT MAX(collection_id)        FROM collections),             0)+1, false);
SELECT setval(pg_get_serial_sequence('collection_items',         'collection_item_id'), COALESCE((SELECT MAX(collection_item_id)   FROM collection_items),        0)+1, false);
SELECT setval(pg_get_serial_sequence('set_stories',              'story_id'),           COALESCE((SELECT MAX(story_id)             FROM set_stories),             0)+1, false);
SELECT setval(pg_get_serial_sequence('achievements',             'id'),                 COALESCE((SELECT MAX(id)                   FROM achievements),            0)+1, false);
SELECT setval(pg_get_serial_sequence('user_achievements',        'id'),                 COALESCE((SELECT MAX(id)                   FROM user_achievements),       0)+1, false);
SELECT setval(pg_get_serial_sequence('pm_point_wallets',         'id'),                 COALESCE((SELECT MAX(id)                   FROM pm_point_wallets),        0)+1, false);
SELECT setval(pg_get_serial_sequence('vouchers',                 'id'),                 COALESCE((SELECT MAX(id)                   FROM vouchers),                0)+1, false);
SELECT setval(pg_get_serial_sequence('user_inventory',           'id'),                 COALESCE((SELECT MAX(id)                   FROM user_inventory),          0)+1, false);
SELECT setval(pg_get_serial_sequence('user_collection_progress', 'id'),                 COALESCE((SELECT MAX(id)                   FROM user_collection_progress),0)+1, false);
SELECT setval(pg_get_serial_sequence('promotions',               'promotion_id'),       COALESCE((SELECT MAX(promotion_id)         FROM promotions),              0)+1, false);

COMMIT;
"""

with open(new_sql_path, "w", encoding="utf-8") as f:
    f.write(sql)
