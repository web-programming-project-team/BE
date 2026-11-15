
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `calendar_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calendar_event` (
  `event_date` date NOT NULL,
  `event_time` time DEFAULT NULL,
  `bookmark_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `event_id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint DEFAULT NULL,
  `job_id` bigint DEFAULT NULL,
  `policy_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `source_type` varchar(30) NOT NULL,
  `memo` varchar(200) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `type` enum('APPLY_END','APPLY_START','COUNSELING','CUSTOM','JOB_DEADLINE','PARTICIPATION') NOT NULL,
  PRIMARY KEY (`event_id`),
  KEY `FK5heri1q06vvxl33p967i7065l` (`bookmark_id`),
  KEY `FKeqqqfd167g35qjvn6gxsqp7d8` (`item_id`),
  KEY `FK4we6jdfuudijs7x9esjgabttj` (`policy_id`),
  KEY `FKbacfttv2921opgo9htv1qmhi9` (`user_id`),
  CONSTRAINT `FK4we6jdfuudijs7x9esjgabttj` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`),
  CONSTRAINT `FK5heri1q06vvxl33p967i7065l` FOREIGN KEY (`bookmark_id`) REFERENCES `job_bookmark` (`bookmark_id`) ON DELETE CASCADE,
  CONSTRAINT `FKbacfttv2921opgo9htv1qmhi9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `FKeqqqfd167g35qjvn6gxsqp7d8` FOREIGN KEY (`item_id`) REFERENCES `roadmap_item` (`item_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_bookmark`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_bookmark` (
  `bookmark_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `job_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`bookmark_id`),
  UNIQUE KEY `UQ_JOB_BOOKMARK_USER_JOB` (`user_id`,`job_id`),
  KEY `FK8yxb2qudqes4adud1sam8shc1` (`job_id`),
  CONSTRAINT `FK8yxb2qudqes4adud1sam8shc1` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`job_id`),
  CONSTRAINT `FKjtno4i5q0t1lgt02hy8iw3d1d` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_policy_match`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_policy_match` (
  `job_id` bigint NOT NULL,
  `policy_id` bigint NOT NULL,
  PRIMARY KEY (`job_id`,`policy_id`),
  KEY `FKc6a4ny21fairgvnras2205c7u` (`policy_id`),
  CONSTRAINT `FKc6a4ny21fairgvnras2205c7u` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`),
  CONSTRAINT `FKf44rdt6miv01l15v9rir8xiet` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_posting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_posting` (
  `closed` bit(1) NOT NULL,
  `deadline` date DEFAULT NULL,
  `collected_at` datetime(6) NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `job_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL,
  `career_level` varchar(30) NOT NULL,
  `external_id` varchar(64) DEFAULT NULL,
  `company` varchar(100) NOT NULL,
  `salary` varchar(100) DEFAULT NULL,
  `address` varchar(200) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `original_url` varchar(500) NOT NULL,
  `education` enum('ANY','COLLEGE','HIGH_SCHOOL','UNIVERSITY') NOT NULL,
  `employment_type` enum('CONTRACT','FULL_TIME','INTERN','PART_TIME') NOT NULL,
  `job_category` enum('DESIGN','ETC','IT','MANUFACTURING','OFFICE','SERVICE') NOT NULL,
  `region` enum('ANSAN','BUCHEON','GWANGMYEONG','INCHEON','SIHEUNG') NOT NULL,
  `source` enum('ADMIN','JOBABA') NOT NULL,
  PRIMARY KEY (`job_id`),
  UNIQUE KEY `UKhgbpu75474h31lh4gxyxyd0w1` (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_preference` (
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  `career_level` varchar(30) DEFAULT NULL,
  `education` enum('ANY','COLLEGE','HIGH_SCHOOL','UNIVERSITY') DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FKqxdg23srnc1oa5suv5j2nft0o` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_preference_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_preference_category` (
  `user_id` bigint NOT NULL,
  `job_category` enum('DESIGN','ETC','IT','MANUFACTURING','OFFICE','SERVICE') NOT NULL,
  PRIMARY KEY (`user_id`,`job_category`),
  CONSTRAINT `FK9m9o7rtxwcdrlxap0pig3utsi` FOREIGN KEY (`user_id`) REFERENCES `job_preference` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_preference_employment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_preference_employment_type` (
  `user_id` bigint NOT NULL,
  `employment_type` enum('CONTRACT','FULL_TIME','INTERN','PART_TIME') NOT NULL,
  PRIMARY KEY (`user_id`,`employment_type`),
  CONSTRAINT `FKkblrc72b9rdopmg5qnl9kpbyw` FOREIGN KEY (`user_id`) REFERENCES `job_preference` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `job_preference_region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_preference_region` (
  `user_id` bigint NOT NULL,
  `region` enum('ANSAN','BUCHEON','GWANGMYEONG','INCHEON','SIHEUNG') NOT NULL,
  PRIMARY KEY (`user_id`,`region`),
  CONSTRAINT `FK1ruplq38nflu0ex5w8uh1im76` FOREIGN KEY (`user_id`) REFERENCES `job_preference` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `is_read` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `target_type` varchar(30) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `body` varchar(300) DEFAULT NULL,
  `type` enum('DEADLINE','NEW_JOB','NEW_REVIEW','SYSTEM') NOT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `IX_NOTIFICATION_USER_READ` (`user_id`,`is_read`),
  CONSTRAINT `FKnk4ftb5am9ubmkv1661h15ds9` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policy` (
  `apply_end_date` date DEFAULT NULL,
  `apply_start_date` date DEFAULT NULL,
  `last_verified_at` date NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `policy_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `external_id` varchar(50) DEFAULT NULL,
  `agency` varchar(100) NOT NULL,
  `contact` varchar(200) DEFAULT NULL,
  `end_reason` varchar(200) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `apply_url` varchar(500) NOT NULL,
  `source_url` varchar(500) NOT NULL,
  `target` varchar(500) NOT NULL,
  `content` text NOT NULL,
  `field` enum('COUNSELING','CULTURE_LIFE','EDUCATION','EMPLOYMENT','FINANCE','HOUSING') NOT NULL,
  `recruit_status` enum('CLOSED','ENDED','OPEN','UPCOMING') NOT NULL,
  `source` enum('ADMIN','ONTONG') NOT NULL,
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `UKd427f83dft248x6692vcrgnrf` (`external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `policy_life_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policy_life_stage` (
  `policy_id` bigint NOT NULL,
  `life_stage` enum('ENROLLED','FIRST_JOB','GRADUATING','INDEPENDENCE','JOB_SEEKING','MILITARY','ON_LEAVE') NOT NULL,
  PRIMARY KEY (`policy_id`,`life_stage`),
  CONSTRAINT `FKstco0fd5bjndob22vcc41wxtl` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `policy_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policy_tag` (
  `policy_id` bigint NOT NULL,
  `tag` varchar(30) NOT NULL,
  PRIMARY KEY (`policy_id`,`tag`),
  CONSTRAINT `FKrsrhrx6ho3kp0lca22w9p63i2` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `hidden` bit(1) NOT NULL,
  `rating` tinyint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `job_id` bigint DEFAULT NULL,
  `policy_id` bigint DEFAULT NULL,
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `one_liner` varchar(60) NOT NULL,
  `hidden_reason` varchar(200) DEFAULT NULL,
  `portfolio_url` varchar(500) DEFAULT NULL,
  `content` text,
  `author_stage` enum('ENROLLED','FIRST_JOB','GRADUATING','INDEPENDENCE','JOB_SEEKING','MILITARY','ON_LEAVE') NOT NULL,
  `author_stage_after` enum('ENROLLED','FIRST_JOB','GRADUATING','INDEPENDENCE','JOB_SEEKING','MILITARY','ON_LEAVE') DEFAULT NULL,
  `job_category` enum('DESIGN','ETC','IT','MANUFACTURING','OFFICE','SERVICE') DEFAULT NULL,
  `prep_period` enum('MONTH_1','MONTH_3','MONTH_6_PLUS','WEEK_1') NOT NULL,
  `type` enum('PASS_REVIEW','PORTFOLIO','PROGRAM_REVIEW') NOT NULL,
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_helpful_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_helpful_point` (
  `post_id` bigint NOT NULL,
  `point` varchar(30) NOT NULL,
  PRIMARY KEY (`post_id`,`point`),
  CONSTRAINT `FKtay585ines807idwg3ue0t60v` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_like` (
  `created_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_report` (
  `created_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `report_id` bigint NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `detail` varchar(200) DEFAULT NULL,
  `reason` enum('ABUSE','ETC','FALSE_INFO','PERSONAL_INFO','SPAM') NOT NULL,
  `status` enum('PENDING','RESOLVED') NOT NULL,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `UQ_POST_REPORT_POST_REPORTER` (`post_id`,`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_token` (
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `token_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token_hash` char(64) NOT NULL,
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `UQ_REFRESH_TOKEN_HASH` (`token_hash`),
  KEY `FKjtx87i0jvq2svedphegvdwcuy` (`user_id`),
  CONSTRAINT `FKjtx87i0jvq2svedphegvdwcuy` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roadmap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roadmap` (
  `archived` bit(1) NOT NULL,
  `archived_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `roadmap_id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `template_name` varchar(100) NOT NULL,
  PRIMARY KEY (`roadmap_id`),
  KEY `FK797psc4deou315h2ytag5cobg` (`template_id`),
  KEY `FKd5i0uf9j7wr1bjchvjgr1vekt` (`user_id`),
  CONSTRAINT `FK797psc4deou315h2ytag5cobg` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`),
  CONSTRAINT `FKd5i0uf9j7wr1bjchvjgr1vekt` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roadmap_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roadmap_item` (
  `user_added` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `policy_id` bigint NOT NULL,
  `roadmap_step_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `status` enum('APPLIED','COMPLETED','INTERESTED','IN_PROGRESS','PLANNED','TO_CHECK') NOT NULL,
  PRIMARY KEY (`item_id`),
  KEY `FK1iiiqne229hiu3su32cmaoiev` (`policy_id`),
  KEY `FKmxhk64b9sy0g532vcltwarkbe` (`roadmap_step_id`),
  CONSTRAINT `FK1iiiqne229hiu3su32cmaoiev` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`),
  CONSTRAINT `FKmxhk64b9sy0g532vcltwarkbe` FOREIGN KEY (`roadmap_step_id`) REFERENCES `roadmap_step` (`roadmap_step_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roadmap_item_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roadmap_item_history` (
  `changed_at` datetime(6) NOT NULL,
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` bigint NOT NULL,
  `from_status` enum('APPLIED','COMPLETED','INTERESTED','IN_PROGRESS','PLANNED','TO_CHECK') DEFAULT NULL,
  `to_status` enum('APPLIED','COMPLETED','INTERESTED','IN_PROGRESS','PLANNED','TO_CHECK') NOT NULL,
  PRIMARY KEY (`history_id`),
  KEY `FKaw1seum8sxbp0bmflma7jvfpv` (`item_id`),
  CONSTRAINT `FKaw1seum8sxbp0bmflma7jvfpv` FOREIGN KEY (`item_id`) REFERENCES `roadmap_item` (`item_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roadmap_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roadmap_step` (
  `step_order` int NOT NULL,
  `roadmap_id` bigint NOT NULL,
  `roadmap_step_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  PRIMARY KEY (`roadmap_step_id`),
  KEY `FKb4bclarl705xs02gvcxydgg2s` (`roadmap_id`),
  CONSTRAINT `FKb4bclarl705xs02gvcxydgg2s` FOREIGN KEY (`roadmap_id`) REFERENCES `roadmap` (`roadmap_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `roadmap_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roadmap_template` (
  `created_at` datetime(6) DEFAULT NULL,
  `template_id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sync_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sync_log` (
  `failed` int NOT NULL,
  `fetched` int NOT NULL,
  `inserted` int NOT NULL,
  `updated` int NOT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `started_at` datetime(6) NOT NULL,
  `sync_log_id` bigint NOT NULL AUTO_INCREMENT,
  `source` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL,
  `trigger_type` varchar(30) NOT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`sync_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `template_interest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_interest` (
  `template_id` bigint NOT NULL,
  `interest_field` enum('COUNSELING','CULTURE_LIFE','EDUCATION','EMPLOYMENT','FINANCE','HOUSING') NOT NULL,
  PRIMARY KEY (`template_id`,`interest_field`),
  CONSTRAINT `FK5wi6bwbchet5gvpuqoc4ddsa5` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `template_life_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_life_stage` (
  `template_id` bigint NOT NULL,
  `life_stage` enum('ENROLLED','FIRST_JOB','GRADUATING','INDEPENDENCE','JOB_SEEKING','MILITARY','ON_LEAVE') NOT NULL,
  PRIMARY KEY (`template_id`,`life_stage`),
  CONSTRAINT `FKawqrm2epcihwaxq10e0v9f528` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `template_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_step` (
  `step_order` int NOT NULL,
  `template_id` bigint NOT NULL,
  `template_step_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  PRIMARY KEY (`template_step_id`),
  KEY `FK7aex9xst9wkd2kpqr81l8c77s` (`template_id`),
  CONSTRAINT `FK7aex9xst9wkd2kpqr81l8c77s` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `template_step_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_step_policy` (
  `policy_id` bigint NOT NULL,
  `template_step_id` bigint NOT NULL,
  PRIMARY KEY (`policy_id`,`template_step_id`),
  KEY `FKk3u285fy7cq67ce5g506r4rse` (`template_step_id`),
  CONSTRAINT `FK550jhpqugpu4jpusowp6rhlnf` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`),
  CONSTRAINT `FKk3u285fy7cq67ce5g506r4rse` FOREIGN KEY (`template_step_id`) REFERENCES `template_step` (`template_step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_interest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_interest` (
  `user_id` bigint NOT NULL,
  `interest_field` enum('COUNSELING','CULTURE_LIFE','EDUCATION','EMPLOYMENT','FINANCE','HOUSING') NOT NULL,
  PRIMARY KEY (`user_id`,`interest_field`),
  CONSTRAINT `FK97yodomof0ffqwpnt9obp1l6c` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_life_stage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_life_stage` (
  `user_id` bigint NOT NULL,
  `life_stage` enum('ENROLLED','FIRST_JOB','GRADUATING','INDEPENDENCE','JOB_SEEKING','MILITARY','ON_LEAVE') NOT NULL,
  PRIMARY KEY (`user_id`,`life_stage`),
  CONSTRAINT `FK4xvfgyfxkydeqql9neihfvdbo` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_visit` (
  `visit_date` date NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`visit_date`,`user_id`),
  KEY `FKrsil33o74ndy0cmod5p0nfvcl` (`user_id`),
  CONSTRAINT `FKrsil33o74ndy0cmod5p0nfvcl` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `agree_privacy` bit(1) NOT NULL,
  `agree_terms` bit(1) NOT NULL,
  `birth_year` smallint DEFAULT NULL,
  `onboarding_completed` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `nickname` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('ADMIN','USER') NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UQ_USERS_EMAIL` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

