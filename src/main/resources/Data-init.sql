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

INSERT INTO achievements (id, name, description, condition_type, condition_value, pm_point_reward, is_hidden) VALUES
(1, 'Collector Novice',  'Sở hữu 5 thẻ bài liên kết',    'CARD_COUNT',         5,  50,  false),
(2, 'Card Master',       'Sở hữu 20 thẻ bài liên kết',   'CARD_COUNT',        20, 200, false),
(3, 'Secret Archivist',  '???',                            'COLLECTION_COMPLETE', 1, 500,  true),
(4, 'First Legend',      'Sở hữu 1 lá LEGENDARY',         'RARITY_COUNT',       1, 100, false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO PROMOTIONS (name, description, discount_type, discount_value, start_date, end_date, created_at, updated_at) VALUES
('Grand Opening Sale', 'Khuyến mãi khai trương PixelMage', 'PERCENTAGE', 15.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ================================================================
-- SECTION 2: ACCOUNTS + CORE CATALOG
-- password is: adminpassword ($2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG)================================================================
INSERT INTO Accounts (customer_id, email, password, name, auth_provider, is_active, email_verified, avatar_url, Role_id, created_at, updated_at) VALUES
(1, 'admin@pixelmage.com',  '$2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG', 'System Admin',  'LOCAL', true, true, 'https://api.dicebear.com/7.x/bottts/svg?seed=admin',  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'staff@pixelmage.com',  '$2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG', 'Staff Member',  'LOCAL', true, true, 'https://api.dicebear.com/7.x/bottts/svg?seed=staff',  2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'user@pixelmage.com',   '$2a$10$pZVTj8enac8omlbdLgypl.2bxHHqGiqFNSr7cvMq3qWbDeoeAvzUG', 'Test User',     'LOCAL', true, true, 'https://api.dicebear.com/7.x/bottts/svg?seed=testuser', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (customer_id) DO NOTHING;

INSERT INTO card_templates (card_template_id, name, description, design_path, arcana_type, suit, card_number, rarity, framework_id, is_active, image_path, created_at, updated_at) VALUES
(1, 'The Fool - Aion Zero', 'Aion Zero là "Kẻ Khởi Hành", xuất hiện ở nơi thời gian chưa chảy. Hành trình của anh đi từ sự ngây thơ ban đầu, trải qua mất mát, cám dỗ và điên loạn để đến với sự giác ngộ. Cuối cùng, anh trưởng thành không phải vì mạnh hơn, mà vì dám chấp nhận hậu quả của các lựa chọn và tiếp tục bước đi.', NULL, 'MAJOR', NULL, 0, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123777/00_e9wsbz.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'The Magician - Chronos Vale', 'Chronos là một nhà tư tưởng duy lý tin rằng thế giới có thể hiểu và sửa chữa. Sau cái chết của người yêu, ông dùng phép thuật can thiệp vào định mệnh để tìm ý nghĩa nhưng lại gây ra sự sụp đổ (The Tower). Cuối cùng, ông hy sinh và nhận ra tự do nằm ở việc chấp nhận những điều phi lý không thể hiểu.', NULL, 'MAJOR', NULL, 1, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123778/01_b9661a.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'The High Priestess - Lunaria Noct', 'Lunaria sinh ra với khả năng nhìn thấy mọi kết cục. Trải qua bi kịch vì nói ra sự thật, cô chọn sự im lặng – một "lòng trắc ẩn chậm rãi". Cô giữ ký ức thay cho The Fool, để anh tự trải nghiệm và trưởng thành thay vì nói trước mọi bề.', NULL, 'MAJOR', NULL, 2, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123777/02_yuw0hm.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'The Empress - Gaia Mater', 'Gaia là hiện thân của sự sống, yêu thương mọi sinh mệnh vô điều kiện. Tuy nhiên, bà nhận ra bi kịch là không thể kiểm soát hay bảo vệ từng cá nhân khỏi cái chết. Qua The Fool, bà học được bài học khó nhất: nuôi dưỡng không phải là kìm kẹp, mà là cho phép họ ra đi và tiếp diễn vòng đời.', NULL, 'MAJOR', NULL, 3, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123777/03_jszosj.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'The Emperor - Aurelion Rex', 'Ám ảnh bởi sự hỗn loạn từ nhỏ, Aurelion dựng lên một đế chế bằng luật lệ và kỷ luật thép. Sự xuất hiện của The Fool làm trật tự này lung lay, khiến ông hoảng sợ và siết chặt kiểm soát. Cuối cùng, khi đế chế sụp đổ, ông hiểu ra trật tự không thể thay thế for trách nhiệm cá nhân.', NULL, 'MAJOR', NULL, 4, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123777/04_bahbwc.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'The Hierophant - Sanctus Verum', 'Sanctus lập ra giáo hội để xoa dịu nỗi đau nhân loại, trao cho họ một ý nghĩa. Nhưng rồi đức tin ấy bị đông cứng thành giáo điều, khiến ông sợ hãi sự tư duy độc lập. The Fool giúp ông nhận ra đạo đức không chỉ đến từ đức tin, và trưởng thành là khi mỗi người tự tìm lấy câu trả lời.', NULL, 'MAJOR', NULL, 5, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123777/05_bwkkhq.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'The Lovers - Elyon & Seris', 'Elyon (tin vào tự do) và Seris (tin vào cộng đồng) yêu nhau vì sự thấu hiểu. Khi chiến tranh nổ ra, họ buộc phải chia tay vì không ai muốn từ bỏ giá trị cốt lõi của mình. Câu chuyện của họ minh chứng rằng tự do là tự chọn thứ mình sẵn sàng đánh mất.', NULL, 'MAJOR', NULL, 6, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123778/06_vpjk5b.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'The Chariot - Kael Vantis', 'Sống trong nền văn hóa tôn sùng chiến thắng, Kael tiến lên không ngừng nghỉ vì sợ bị bỏ lại. Cho đến khi gặp The Fool, anh mới dám trả lời câu hỏi "Có mệt không?" và học được quyền được dừng lại thay vì tự hành hạ ý chí.', NULL, 'MAJOR', NULL, 7, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123778/07_f9iy90.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Strength - Lyra Fero', 'Mang trong mình sức mạnh áp đảo, Lyra luôn tự đè nén vì sợ làm tổn thương người khác, biến sự nhường nhịn thành tự hủy hoại. The Fool giúp cô hiểu rằng sức mạnh thật sự không nằm ở sự yếu đuối, mà ở khả năng thuần phục bản ngã: có thể làm tổn thương nhưng chọn không làm vậy.', NULL, 'MAJOR', NULL, 8, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123778/08_hxrklx.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'The Hermit - Orion Solus', 'Orion từng là người ở đỉnh cao danh vọng nhưng rút lui vào cô độc vì thấy sự thật luôn bị bóp méo. Ông trao cho The Fool ngọn đèn không phải để soi đường, mà để truyền lại cách đặt câu hỏi và dám ở một mình với nhận thức.', NULL, 'MAJOR', NULL, 9, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123778/09_n4a8ru.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Wheel of Fortune - Tycho Rhei', 'Tycho là kẻ đẩy bánh xe số phận, nhìn thấy mọi xác suất và quy luật của vạn vật. Ông từng sợ hãi sự ngẫu nhiên vô tình, nhưng hiểu ra nếu không có ngẫu nhiên, thế giới sẽ trì trệ. Bài học ông trao là: "Bạn không chọn được lá bài… nhưng bạn luôn chọn cách chơi".', NULL, 'MAJOR', NULL, 10, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123778/10_z571ba.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Justice - Verena Caelum', 'Là người thực thi công lý, Verena luôn phải đối mặt with những phán quyết không thể làm hài lòng tất cả và đôi khi gây tổn thương người vô tội. Cô dạy The Fool rằng công lý không phải là lòng tốt, mà là giới hạn cần thiết và phải chịu trách nhiệm for cả quyết định đúng mà đau đớn.', NULL, 'MAJOR', NULL, 11, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123779/11_gx2vfr.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'The Hanged Man - Elias Vale', 'Một nhà cải cách xã hội tin rằng lý trí và sự đúng đắn sẽ thay đổi được hệ thống tham nhũng. Khi bị đồng minh phản bội, ông chọn cách "treo mình" – chấp nhận làm vật tế thần, chịu lãng quên để hệ thống không sụp đổ làm hại người vô tội.', NULL, 'MAJOR', NULL, 12, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123779/12_u3idsa.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Death - Mortis Nox', 'Bị vạn vật nguyền rủa, Mortis Nox là người đóng cửa kỷ nguyên. Ông không thể do dự, vì sự sống nếu cứ kéo dài vô nghĩa sẽ sinh ra đau đớn. Gặp The Fool – người không sợ hãi cái chết, Mortis chứng minh rằng kết thúc là sự giải phóng để bảo vệ tái sinh.', NULL, 'MAJOR', NULL, 13, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123779/13_s6d6jw.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Temperance - Seraphine Calyx', 'Sinh ra giữa Ngã Ba Althyr của các phe đối lập, Seraphine chọn làm người hòa giải và pha chế các mâu thuẫn. Bị xem là kẻ phản bội vì không đứng về phe nào, nhưng cô vẫn kiên trì tạo ra trạng thái tồn tại: "Cân bằng không phải đứng yên, mà là điều chỉnh liên tục".', NULL, 'MAJOR', NULL, 14, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123780/14_pbwl2k.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'The Devil - Asmodeus Vex', 'Asmodeus sinh ra khi con người lần đầu tự dối mình. Hắn không trói buộc ai mà chỉ phơi bày những ham muốn sâu thẳm nhất. The Fool chiến thắng hắn bằng cách nhìn nhận rõ dục vọng nhưng không chọn nó, chứng minh tự do là không bị ham muốn điều khiển.', NULL, 'MAJOR', NULL, 15, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123780/15_a7uk58.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'The Tower - Cassius Pyr', 'Cassius là kiến trúc sư vĩ đại xây nên Tòa Tháp của các hệ thống tư tưởng, nhưng xây trên nền móng của sự trốn tránh và phủ nhận sai lầm. Khi tháp sụp đổ, ông nhận ra thức tỉnh không phải là thấy ánh sáng, mà là lúc không thể giả vờ ngủ nữa.', NULL, 'MAJOR', NULL, 16, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123782/16_k7tfbg.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'The Star - Lyrae Lume', 'Sinh ra từ tàn tích của Tòa Tháp sụp đổ, hy vọng của Lyrae không ồn ào mà tồn tại qua những hành động gieo mầm nhỏ nhặt. Cô trao cho The Fool sự bền bỉ, nhắc nhở anh rằng hy vọng là sự lựa chọn phải lặp lại mỗi ngày để sống tiếp in bóng tối.', NULL, 'MAJOR', NULL, 17, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123781/17_ssdiue.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'The Moon - Nox Eir', 'Sinh ra từ tiềm thức và bóng tối tâm lý, Nox khuếch đại nỗi sợ khiến con người mắc kẹt in ảo ảnh của chính mình. Bài học của The Moon là phải dám nghi ngờ cả trực giác và cảm xúc, vì chúng thường là tiếng vang of nỗi sợ cũ chứ không phải sự thật.', NULL, 'MAJOR', NULL, 18, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123782/18_jgwqlv.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'The Sun - Solen Verus', 'Solen là ánh sáng phơi bày vạn vật, không bao giờ nói dối và từ chối an ủi bằng hy vọng hão huyền. Dù bị ghét vì quá tàn nhẫn, ông dạy The Fool rằng sự thật tuy đau nhưng giúp ta đứng thẳng, và dối trá nhân từ cũng là bạo lực.', NULL, 'MAJOR', NULL, 19, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123782/19_nvi9px.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Judgement - Elias Reso', 'Elias không phán xét bằng luật mà là tiếng vọng từ chính ký ức và hậu quả từ những lựa chọn of mỗi người. The Fool đối diện with ông bằng cách chịu nhận mọi trách nhiệm, qua đó nhận ra tha thứ chỉ bắt đầu khi ta ngừng đổ lỗi.', NULL, 'MAJOR', NULL, 20, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123782/20_rdmsms.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'The World - Orbis Aeter', 'Orbis không phải là đích đến hoàn hảo, mà là trạng thái vạn vật không còn chống lại nhau. The Fool gặp Orbis không mang tham vọng tìm phần thưởng. Anh nhận ra trưởng thành là thôi chống lại chính mình, để sau đó vòng tròn lặp lại Oroboros khởi động lần nữa nhưng with chiều sâu trải nghiệm hoàn toàn mới.', NULL, 'MAJOR', NULL, 21, 'LEGENDARY', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1775123783/21_rnqpll.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Ace of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 1, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/ace.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Two of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 2, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/two.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Three of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 3, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/three.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Four of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 4, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/four.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'Five of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 5, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/five.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'Six of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 6, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/six.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'Seven of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 7, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/seven.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'Eight of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 8, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/eight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 'Nine of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 9, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/nine.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 'Ten of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 10, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/ten.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(33, 'Page of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 11, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/page.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(34, 'Knight of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 12, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/knight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(35, 'Queen of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 13, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/queen.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(36, 'King of Wands', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'WANDS', 14, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/wands/king.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(37, 'Ace of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 1, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/ace.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(38, 'Two of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 2, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/two.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39, 'Three of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 3, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/three.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40, 'Four of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 4, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/four.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, 'Five of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 5, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/five.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(42, 'Six of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 6, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/six.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(43, 'Seven of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 7, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/seven.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(44, 'Eight of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 8, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/eight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(45, 'Nine of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 9, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/nine.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(46, 'Ten of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 10, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/ten.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(47, 'Page of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 11, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/page.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(48, 'Knight of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 12, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/knight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(49, 'Queen of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 13, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/queen.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(50, 'King of Cups', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'CUPS', 14, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/cups/king.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(51, 'Ace of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 1, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/ace.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(52, 'Two of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 2, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/two.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(53, 'Three of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 3, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/three.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(54, 'Four of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 4, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/four.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(55, 'Five of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 5, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/five.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(56, 'Six of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 6, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/six.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(57, 'Seven of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 7, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/seven.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(58, 'Eight of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 8, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/eight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(59, 'Nine of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 9, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/nine.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(60, 'Ten of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 10, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/ten.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(61, 'Page of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 11, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/page.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(62, 'Knight of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 12, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/knight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(63, 'Queen of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 13, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/queen.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(64, 'King of Swords', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'SWORDS', 14, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/swords/king.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(65, 'Ace of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 1, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/ace.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(66, 'Two of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 2, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/two.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(67, 'Three of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 3, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/three.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(68, 'Four of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 4, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/four.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(69, 'Five of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 5, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/five.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(70, 'Six of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 6, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/six.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(71, 'Seven of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 7, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/seven.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(72, 'Eight of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 8, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/eight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(73, 'Nine of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 9, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/nine.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(74, 'Ten of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 10, 'COMMON', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/ten.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(75, 'Page of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 11, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/page.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(76, 'Knight of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 12, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/knight.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(77, 'Queen of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 13, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/queen.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(78, 'King of Pentacles', 'Năng lượng nguyên tố thuần túy, không gắn liền with nhân vật cụ thể in truyền thuyết PixelMage.', NULL, 'MINOR', 'PENTACLES', 14, 'RARE', 1, true, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/cards/minor/pentacles/king.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (card_template_id) DO NOTHING;

-- Sample Content for Card Templates (The Fool, The Magician, The High Priestess)
INSERT INTO card_content (card_template_id, title, content_type, content_data, display_order, is_active, created_at, updated_at) VALUES
-- The Fool (ID 1)
(1, 'Hành Trình Của Aion Zero', 'STORY', 'Aion Zero không phải là một anh hùng, anh là hiện thân của sự khởi đầu. Tại rìa của chiều không gian thứ 0, nơi thời gian chưa bao giờ chảy, anh bước đi với tâm thế của một kẻ ngây thơ, không ký ức, không sợ hãi. Mỗi bước chân của anh tạo ra một nhịp đập cho vũ trụ. Hành trình của anh không phải là tìm kiếm quyền năng, mà là trải nghiệm mọi cung bậc của sự tồn tại: từ niềm vui thuần khiết đến nỗi đau xé lòng, từ sự cám dỗ của quỷ dữ đến sự thanh thản của các vị thần. Anh là kẻ duy nhất dám chấp nhận hậu quả của các lựa chọn mà không bao giờ hối tiếc, vĩnh viễn bước về phía trước cho đến khi vòng lặp bắt đầu lại.', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'The Journey Begins', 'IMAGE', 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/samples/fool-journey.jpg', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- The Magician (ID 2)
(2, 'Nhật Ký Của Chronos Vale', 'STORY', 'Chronos Vale từng tin rằng thế giới là một cỗ máy có thể sửa chữa bằng logic và đại số cổ đại. Khi mất đi người mình yêu nhất, ông đã phạm phải điều cấm kỵ: dùng phép thuật bóp méo thời gian để cứu vãn quá khứ. Kết quả là sự sụp đổ của Tòa Tháp (The Tower) và sự hỗn loạn lan rộng khắp bình diện. Sau hàng thế kỷ nghiên cứu, Chronos nhận ra rằng sức mạnh thật sự không nằm ở việc điều khiển định mệnh, mà ở việc thấu hiểu quy luật tự nhiên. Cuốn nhật ký của ông ghi lại sự chuyển biến từ một kẻ ngạo mạn muốn thay đổi thế giới thành một hiền triết biết cúi đầu trước những điều phi lý của vũ trụ. Phép thuật của ông giờ đây không dùng để áp đặt, mà để bảo vệ sự tự do của mọi sinh mệnh.', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'The Great Work', 'VIDEO', 'https://res.cloudinary.com/yukiimin-cloud/video/upload/v1/samples/magician-ritual.mp4', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- The High Priestess (ID 3)
(3, 'Truyền Thuyết Lunaria Noct', 'STORY', 'Lunaria Noct không sinh ra từ ánh sáng, mà từ khoảng lặng giữa hai nhịp đập của thời gian. Là người canh giữ Thư Viện Ký Ức (The Great Archive), cô sở hữu đôi mắt có thể nhìn thấu mọi kết cục của một sự việc trước khi nó bắt đầu. Tuy nhiên, bi kịch lớn nhất của Lunaria chính là nhận ra rằng việc biết trước tương lai không đồng nghĩa với việc có thể thay đổi nó mà không gây ra sự sụp đổ lớn hơn. Cô chọn khoác lên mình tấm áo của sự im lặng, đứng sau tấm màn của thực tại để dẫn lối cho The Fool. Cô không chỉ dạy anh cách nhìn, mà còn dạy anh cách chấp nhận những bí ẩn không lời giải. Cuốn sách trên tay cô không ghi chép những gì đã xảy ra, mà là những gì mỗi linh hồn đã cảm nhận – vì với Lunaria, cảm xúc là thứ duy nhất thực sự tồn tại vĩnh hằng trong dòng chảy hư ảo.', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Whispers of the Void', 'VIDEO', 'https://res.cloudinary.com/yukiimin-cloud/video/upload/v1/samples/high-priestess-lore.mp4', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO divine_helpers(divine_helper_id, card_template_id, upright_meaning, reversed_meaning, zodiac_sign, element, keywords, created_at, updated_at) VALUES
(1, 1, 'Khởi đầu mới, tự do, mạo hiểm', 'Bất cẩn, liều lĩnh', 'Không quy định', 'Không Khí (Air)', 'Khởi đầu mới, tự do, mạo hiểm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'Ý chí, sức mạnh, hiện thực hoá', 'Thao túng, tiềm năng lãng phí', 'Không quy định', 'Không Khí (Air)', 'Ý chí, sức mạnh, hiện thực hoá', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 'Trực giác, bí ẩn, vô thức', 'Bí mật, cắt đứt cảm xúc', 'Không quy định', 'Nước (Water)', 'Trực giác, bí ẩn, vô thức', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, 'Phong phú, sinh sản, nuôi dưỡng', 'Phụ thuộc, trì trệ sáng tạo', 'Không quy định', 'Đất (Earth)', 'Phong phú, sinh sản, nuôi dưỡng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 5, 'Quyền uy, cấu trúc, ổn định', 'Cứng nhắc, kiểm soát thái quá', 'Bạch Dương (Aries)', 'Lửa (Fire)', 'Quyền uy, cấu trúc, ổn định', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 6, 'Truyền thống, tâm linh, hướng dẫn', 'Giáo điều, phản kháng', 'Kim Ngưu (Taurus)', 'Đất (Earth)', 'Truyền thống, tâm linh, hướng dẫn', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 7, 'Tình yêu, lựa chọn, sự kết hợp', 'Mất cân bằng, lựa chọn sai', 'Song Tử (Gemini)', 'Không Khí (Air)', 'Tình yêu, lựa chọn, sự kết hợp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 8, 'Ý chí, kiểm soát, chiến thắng', 'Thiếu kiểm soát, hung hăng', 'Cự Giải (Cancer)', 'Nước (Water)', 'Ý chí, kiểm soát, chiến thắng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 9, 'Dũng cảm, kiên nhẫn, nội lực', 'Yếu đuối, nghi ngờ bản thân', 'Sư Tử (Leo)', 'Lửa (Fire)', 'Dũng cảm, kiên nhẫn, nội lực', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 10, 'Nội tâm, khôn ngoan, cô độc', 'Cô lập, rút lui', 'Xử Nữ (Virgo)', 'Đất (Earth)', 'Nội tâm, khôn ngoan, cô độc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 11, 'Vận may, chu kỳ, số phận', 'Vận xui, kháng cự thay đổi', 'Không quy định', 'Lửa (Fire)', 'Vận may, chu kỳ, số phận', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 12, 'Công bằng, sự thật, nhân quả', 'Bất công, vô trách nhiệm', 'Thiên Bình (Libra)', 'Không Khí (Air)', 'Công bằng, sự thật, nhân quả', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 13, 'Từ bỏ, góc nhìn mới, hy sinh', 'Trì hoãn, tuẫn tiết vô nghĩa', 'Không quy định', 'Nước (Water)', 'Từ bỏ, góc nhìn mới, hy sinh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 14, 'Kết thúc, chuyển hoá, tái sinh', 'Kháng cự thay đổi, trì trệ', 'Bọ Cạp (Scorpio)', 'Nước (Water)', 'Kết thúc, chuyển hoá, tái sinh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 15, 'Cân bằng, kiên nhẫn, điều độ', 'Mất cân bằng, thái quá', 'Nhân Mã (Sagittarius)', 'Lửa (Fire)', 'Cân bằng, kiên nhẫn, điều độ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 16, 'Ràng buộc, vật chất, bóng tối', 'Giải thoát, nhận thức', 'Ma Kết (Capricorn)', 'Đất (Earth)', 'Ràng buộc, vật chất, bóng tối', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 17, 'Sụp đổ đột ngột, khải thị', 'Tránh né thảm họa, trì hoãn', 'Không quy định', 'Lửa (Fire)', 'Sụp đổ đột ngột, khải thị', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 18, 'Hy vọng, cảm hứng, chữa lành', 'Tuyệt vọng, mất niềm tin', 'Bảo Bình (Aquarius)', 'Không Khí (Air)', 'Hy vọng, cảm hứng, chữa lành', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 19, 'Ảo tưởng, sợ hãi, vô thức', 'Giải phóng sợ hãi, rõ ràng hơn', 'Song Ngư (Pisces)', 'Nước (Water)', 'Ảo tưởng, sợ hãi, vô thức', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 20, 'Vui vẻ, thành công, sức sống', 'Tạm thời, lạc quan thái quá', 'Không quy định', 'Lửa (Fire)', 'Vui vẻ, thành công, sức sống', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 21, 'Thức tỉnh, tha thứ, tái sinh', 'Tự phê bình, nghi ngờ', 'Không quy định', 'Lửa (Fire)', 'Thức tỉnh, tha thứ, tái sinh', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 22, 'Hoàn thành, tích hợp, thành tựu', 'Dang dở, thiếu kết thúc', 'Không quy định', 'Đất (Earth)', 'Hoàn thành, tích hợp, thành tựu', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 23, 'Khởi đầu mới, cơ hội, cảm hứng mang đam mê, hành động, sáng tạo', 'Cơ hội bị bỏ lỡ, trì hoãn', NULL, 'Lửa (Fire)', 'Khởi đầu, Tiềm năng, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 24, 'Lựa chọn, cân bằng, đối tác mang đam mê, hành động, sáng tạo', 'Mất cân bằng, quyết định tăm tối', NULL, 'Lửa (Fire)', 'Cân bằng, Lựa chọn, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 25, 'Phát triển, hợp tác, mở rộng mang đam mê, hành động, sáng tạo', 'Trì hoãn, làm việc nhóm kém', NULL, 'Lửa (Fire)', 'Hợp tác, Phát triển, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 26, 'Ổn định, nền tảng, an toàn mang đam mê, hành động, sáng tạo', 'Bất ổn, nền tảng lung lay', NULL, 'Lửa (Fire)', 'Ổn định, Nền tảng, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 27, 'Xung đột, thay đổi, khó khăn mang đam mê, hành động, sáng tạo', 'Giải quyết xung đột, học từ thất bại', NULL, 'Lửa (Fire)', 'Xung đột, Thách thức, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 28, 'Hòa hợp, chiến thắng, ký ức mang đam mê, hành động, sáng tạo', 'Mất động lực, níu kéo quá khứ', NULL, 'Lửa (Fire)', 'Hòa hợp, Phục hồi, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 29, 'Đánh giá, kiên nhẫn, chiến lược mang đam mê, hành động, sáng tạo', 'Thiếu định hướng, do dự', NULL, 'Lửa (Fire)', 'Kiên nhẫn, Đánh giá, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 30, 'Hành động, thay đổi, kỹ năng mang đam mê, hành động, sáng tạo', 'Trì trệ, mất tập trung', NULL, 'Lửa (Fire)', 'Hành động, Tiến bộ, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 31, 'Viên mãn, tự hào, độc lập mang đam mê, hành động, sáng tạo', 'Tự mãn, khao khát xa vời', NULL, 'Lửa (Fire)', 'Thành tựu, Viên mãn, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 32, 'Hoàn thành, kết thúc chu kỳ, phần thưởng mang đam mê, hành động, sáng tạo', 'Kết thúc buồn, gánh nặng', NULL, 'Lửa (Fire)', 'Hoàn thành, Gánh nặng, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(33, 33, 'Khám phá, tin tức, học hỏi mang đam mê, hành động, sáng tạo', 'Thiếu trưởng thành, tin xấu', NULL, 'Lửa (Fire)', 'Tin tức, Khám phá, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(34, 34, 'Hành động, quyết tâm, năng lượng mang đam mê, hành động, sáng tạo', 'Bốc đồng, mất phương hướng', NULL, 'Lửa (Fire)', 'Hành động, Giải pháp, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(35, 35, 'Nuôi dưỡng, trực giác, tĩnh lặng mang đam mê, hành động, sáng tạo', 'Khép kín, cảm xúc bất ổn', NULL, 'Lửa (Fire)', 'Trực giác, Chăm sóc, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(36, 36, 'Lãnh đạo, kiểm soát, trưởng thành mang đam mê, hành động, sáng tạo', 'Độc đoán, lạm quyền', NULL, 'Lửa (Fire)', 'Lãnh đạo, Quyền lực, Đam mê, Hành động, Sáng tạo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(37, 37, 'Khởi đầu mới, cơ hội, cảm hứng mang cảm xúc, mối quan hệ, trực giác', 'Cơ hội bị bỏ lỡ, trì hoãn', NULL, 'Nước (Water)', 'Khởi đầu, Tiềm năng, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(38, 38, 'Lựa chọn, cân bằng, đối tác mang cảm xúc, mối quan hệ, trực giác', 'Mất cân bằng, quyết định tăm tối', NULL, 'Nước (Water)', 'Cân bằng, Lựa chọn, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39, 39, 'Phát triển, hợp tác, mở rộng mang cảm xúc, mối quan hệ, trực giác', 'Trì hoãn, làm việc nhóm kém', NULL, 'Nước (Water)', 'Hợp tác, Phát triển, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40, 40, 'Ổn định, nền tảng, an toàn mang cảm xúc, mối quan hệ, trực giác', 'Bất ổn, nền tảng lung lay', NULL, 'Nước (Water)', 'Ổn định, Nền tảng, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, 41, 'Xung đột, thay đổi, khó khăn mang cảm xúc, mối quan hệ, trực giác', 'Giải quyết xung đột, học từ thất bại', NULL, 'Nước (Water)', 'Xung đột, Thách thức, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(42, 42, 'Hòa hợp, chiến thắng, ký ức mang cảm xúc, mối quan hệ, trực giác', 'Mất động lực, níu kéo quá khứ', NULL, 'Nước (Water)', 'Hòa hợp, Phục hồi, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(43, 43, 'Đánh giá, kiên nhẫn, chiến lược mang cảm xúc, mối quan hệ, trực giác', 'Thiếu định hướng, do dự', NULL, 'Nước (Water)', 'Kiên nhẫn, Đánh giá, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(44, 44, 'Hành động, thay đổi, kỹ năng mang cảm xúc, mối quan hệ, trực giác', 'Trì trệ, mất tập trung', NULL, 'Nước (Water)', 'Hành động, Tiến bộ, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(45, 45, 'Viên mãn, tự hào, độc lập mang cảm xúc, mối quan hệ, trực giác', 'Tự mãn, khao khát xa vời', NULL, 'Nước (Water)', 'Thành tựu, Viên mãn, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(46, 46, 'Hoàn thành, kết thúc chu kỳ, phần thưởng mang cảm xúc, mối quan hệ, trực giác', 'Kết thúc buồn, gánh nặng', NULL, 'Nước (Water)', 'Hoàn thành, Gánh nặng, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(47, 47, 'Khám phá, tin tức, học hỏi mang cảm xúc, mối quan hệ, trực giác', 'Thiếu trưởng thành, tin xấu', NULL, 'Nước (Water)', 'Tin tức, Khám phá, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(48, 48, 'Hành động, quyết tâm, năng lượng mang cảm xúc, mối quan hệ, trực giác', 'Bốc đồng, mất phương hướng', NULL, 'Nước (Water)', 'Hành động, Giải pháp, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(49, 49, 'Nuôi dưỡng, trực giác, tĩnh lặng mang cảm xúc, mối quan hệ, trực giác', 'Khép kín, cảm xúc bất ổn', NULL, 'Nước (Water)', 'Trực giác, Chăm sóc, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(50, 50, 'Lãnh đạo, kiểm soát, trưởng thành mang cảm xúc, mối quan hệ, trực giác', 'Độc đoán, lạm quyền', NULL, 'Nước (Water)', 'Lãnh đạo, Quyền lực, Cảm xúc, Mối quan hệ, Trực giác', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(51, 51, 'Khởi đầu mới, cơ hội, cảm hứng mang tư duy, lý trí, xung đột', 'Cơ hội bị bỏ lỡ, trì hoãn', NULL, 'Không Khí (Air)', 'Khởi đầu, Tiềm năng, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(52, 52, 'Lựa chọn, cân bằng, đối tác mang tư duy, lý trí, xung đột', 'Mất cân bằng, quyết định tăm tối', NULL, 'Không Khí (Air)', 'Cân bằng, Lựa chọn, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(53, 53, 'Phát triển, hợp tác, mở rộng mang tư duy, lý trí, xung đột', 'Trì hoãn, làm việc nhóm kém', NULL, 'Không Khí (Air)', 'Hợp tác, Phát triển, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(54, 54, 'Ổn định, nền tảng, an toàn mang tư duy, lý trí, xung đột', 'Bất ổn, nền tảng lung lay', NULL, 'Không Khí (Air)', 'Ổn định, Nền tảng, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(55, 55, 'Xung đột, thay đổi, khó khăn mang tư duy, lý trí, xung đột', 'Giải quyết xung đột, học từ thất bại', NULL, 'Không Khí (Air)', 'Xung đột, Thách thức, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(56, 56, 'Hòa hợp, chiến thắng, ký ức mang tư duy, lý trí, xung đột', 'Mất động lực, níu kéo quá khứ', NULL, 'Không Khí (Air)', 'Hòa hợp, Phục hồi, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(57, 57, 'Đánh giá, kiên nhẫn, chiến lược mang tư duy, lý trí, xung đột', 'Thiếu định hướng, do dự', NULL, 'Không Khí (Air)', 'Kiên nhẫn, Đánh giá, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(58, 58, 'Hành động, thay đổi, kỹ năng mang tư duy, lý trí, xung đột', 'Trì trệ, mất tập trung', NULL, 'Không Khí (Air)', 'Hành động, Tiến bộ, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(59, 59, 'Viên mãn, tự hào, độc lập mang tư duy, lý trí, xung đột', 'Tự mãn, khao khát xa vời', NULL, 'Không Khí (Air)', 'Thành tựu, Viên mãn, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(60, 60, 'Hoàn thành, kết thúc chu kỳ, phần thưởng mang tư duy, lý trí, xung đột', 'Kết thúc buồn, gánh nặng', NULL, 'Không Khí (Air)', 'Hoàn thành, Gánh nặng, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(61, 61, 'Khám phá, tin tức, học hỏi mang tư duy, lý trí, xung đột', 'Thiếu trưởng thành, tin xấu', NULL, 'Không Khí (Air)', 'Tin tức, Khám phá, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(62, 62, 'Hành động, quyết tâm, năng lượng mang tư duy, lý trí, xung đột', 'Bốc đồng, mất phương hướng', NULL, 'Không Khí (Air)', 'Hành động, Giải pháp, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(63, 63, 'Nuôi dưỡng, trực giác, tĩnh lặng mang tư duy, lý trí, xung đột', 'Khép kín, cảm xúc bất ổn', NULL, 'Không Khí (Air)', 'Trực giác, Chăm sóc, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(64, 64, 'Lãnh đạo, kiểm soát, trưởng thành mang tư duy, lý trí, xung đột', 'Độc đoán, lạm quyền', NULL, 'Không Khí (Air)', 'Lãnh đạo, Quyền lực, Tư duy, Lý trí, Xung đột', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(65, 65, 'Khởi đầu mới, cơ hội, cảm hứng mang vật chất, thực tế, sức khỏe', 'Cơ hội bị bỏ lỡ, trì hoãn', NULL, 'Đất (Earth)', 'Khởi đầu, Tiềm năng, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(66, 66, 'Lựa chọn, cân bằng, đối tác mang vật chất, thực tế, sức khỏe', 'Mất cân bằng, quyết định tăm tối', NULL, 'Đất (Earth)', 'Cân bằng, Lựa chọn, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(67, 67, 'Phát triển, hợp tác, mở rộng mang vật chất, thực tế, sức khỏe', 'Trì hoãn, làm việc nhóm kém', NULL, 'Đất (Earth)', 'Hợp tác, Phát triển, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(68, 68, 'Ổn định, nền tảng, an toàn mang vật chất, thực tế, sức khỏe', 'Bất ổn, nền tảng lung lay', NULL, 'Đất (Earth)', 'Ổn định, Nền tảng, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(69, 69, 'Xung đột, thay đổi, khó khăn mang vật chất, thực tế, sức khỏe', 'Giải quyết xung đột, học từ thất bại', NULL, 'Đất (Earth)', 'Xung đột, Thách thức, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(70, 70, 'Hòa hợp, chiến thắng, ký ức mang vật chất, thực tế, sức khỏe', 'Mất động lực, níu kéo quá khứ', NULL, 'Đất (Earth)', 'Hòa hợp, Phục hồi, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(71, 71, 'Đánh giá, kiên nhẫn, chiến lược mang vật chất, thực tế, sức khỏe', 'Thiếu định hướng, do dự', NULL, 'Đất (Earth)', 'Kiên nhẫn, Đánh giá, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(72, 72, 'Hành động, thay đổi, kỹ năng mang vật chất, thực tế, sức khỏe', 'Trì trệ, mất tập trung', NULL, 'Đất (Earth)', 'Hành động, Tiến bộ, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(73, 73, 'Viên mãn, tự hào, độc lập mang vật chất, thực tế, sức khỏe', 'Tự mãn, khao khát xa vời', NULL, 'Đất (Earth)', 'Thành tựu, Viên mãn, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(74, 74, 'Hoàn thành, kết thúc chu kỳ, phần thưởng mang vật chất, thực tế, sức khỏe', 'Kết thúc buồn, gánh nặng', NULL, 'Đất (Earth)', 'Hoàn thành, Gánh nặng, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(75, 75, 'Khám phá, tin tức, học hỏi mang vật chất, thực tế, sức khỏe', 'Thiếu trưởng thành, tin xấu', NULL, 'Đất (Earth)', 'Tin tức, Khám phá, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(76, 76, 'Hành động, quyết tâm, năng lượng mang vật chất, thực tế, sức khỏe', 'Bốc đồng, mất phương hướng', NULL, 'Đất (Earth)', 'Hành động, Giải pháp, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(77, 77, 'Nuôi dưỡng, trực giác, tĩnh lặng mang vật chất, thực tế, sức khỏe', 'Khép kín, cảm xúc bất ổn', NULL, 'Đất (Earth)', 'Trực giác, Chăm sóc, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(78, 78, 'Lãnh đạo, kiểm soát, trưởng thành mang vật chất, thực tế, sức khỏe', 'Độc đoán, lạm quyền', NULL, 'Đất (Earth)', 'Lãnh đạo, Quyền lực, Vật chất, Thực tế, Sức khỏe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (divine_helper_id) DO NOTHING;

-- ================================================================
-- SECTION 3: PRODUCTS + INVENTORY
-- ================================================================
INSERT INTO PRODUCTS (product_id, name, description, price, image_url, created_at, updated_at) VALUES
(1, 'PixelMage Standard Pack', '5 lá ngẫu nhiên. Cam kết ít nhất 1 RARE ở Slot 4. Tỷ lệ LEGENDARY Slot 5: 15%.', 79000.00, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/packs/standard-pack.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'PixelMage Blister Promo', '3 Standard Packs + 1 lá Promo RARE đặc biệt.', 199000.00, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/packs/blister-promo.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Major Sealed Box', '10 Standard Packs (50 lá). Đảm bảo ít nhất 1 lá LEGENDARY trong hộp.', 699000.00, 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/packs/major-sealed-box.webp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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

INSERT INTO vouchers (code, discount_pct, max_discount_vnd, owner_id, created_at, expires_at, is_used) VALUES
('PM-TEST-ABCD-1234', 10, 20000, 3, CURRENT_TIMESTAMP, '2026-10-01 23:59:59', false),
('PM-TEST-USED-5678', 10, 20000, 3, CURRENT_TIMESTAMP, '2026-10-01 23:59:59', true)
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
(1, 'Zero Arcana Saga', 'Khi người chơi sở hữu đủ 3 lá: The Fool, The Magician, The High Priestess, họ mở khóa câu chuyện khởi nguồn của hành trình Aion Zero.', '[1,2,3]', 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/stories/zero-arcana.webp', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Foundations of the Court', 'Mở khóa khi sở hữu đủ 4 lá Court of Wands.', '[33,34,35,36]', 'https://res.cloudinary.com/yukiimin-cloud/image/upload/v1/stories/court-of-wands.webp', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
SELECT setval(pg_get_serial_sequence('card_content',             'content_id'),         COALESCE((SELECT MAX(content_id)           FROM card_content),            0)+1, false);

