-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: letucoj
-- ------------------------------------------------------
-- Server version	9.4.0

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

--
-- Current Database: `letucoj`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `letucoj` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `letucoj`;

--
-- Table structure for table `contest`
--

DROP TABLE IF EXISTS `contest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest` (
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `mode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `start` timestamp NOT NULL,
  `end` timestamp NOT NULL,
  `ispublic` tinyint(1) NOT NULL,
  `note` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest`
--

LOCK TABLES `contest` WRITE;
/*!40000 ALTER TABLE `contest` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contest_board`
--

DROP TABLE IF EXISTS `contest_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_board` (
  `contest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `problem_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int NOT NULL,
  `times` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_board`
--

LOCK TABLES `contest_board` WRITE;
/*!40000 ALTER TABLE `contest_board` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contest_problems`
--

DROP TABLE IF EXISTS `contest_problems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contest_problems` (
  `contest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `problem_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `score` int NOT NULL,
  PRIMARY KEY (`contest_name`,`problem_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contest_problems`
--

LOCK TABLES `contest_problems` WRITE;
/*!40000 ALTER TABLE `contest_problems` DISABLE KEYS */;
/*!40000 ALTER TABLE `contest_problems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `problem`
--

DROP TABLE IF EXISTS `problem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `problem` (
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `caseAmount` int NOT NULL,
  `difficulty` int NOT NULL,
  `tags` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `authors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `createtime` datetime NOT NULL,
  `updateat` datetime DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `freq` float DEFAULT NULL,
  `ispublic` tinyint(1) DEFAULT NULL,
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `showsolution` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `problem`
--

LOCK TABLES `problem` WRITE;
/*!40000 ALTER TABLE `problem` DISABLE KEYS */;
INSERT INTO `problem` VALUES ('t1','数组排序和_测试',10,1,'数组,排序','LeetCode','2025-07-01 10:00:00','2025-07-05 15:00:00','给定一个整数数组 nums 和一个目标值 target，找到数组中和为 target 的两个数。',0.8,1,'使用双指针方法，时间复杂度 O(n)',1),('t2','字符串匹配_测试',15,2,'字符串,匹配','LeetCode','2025-07-02 11:00:00','2025-07-06 16:00:00','给定一个字符串 s 和一个模式 p，判断 s 是否与 p 匹配。',0.7,1,'使用动态规划，状态转移矩阵优化',1),('t3','动态规划最大子序列和_测试',12,3,'动态规划,子序列','LeetCode','2025-07-03 12:00:00','2025-07-07 17:00:00','给定一个整数数组 nums，找到具有最大和的连续子数组。',0.6,1,'使用动态规划，时间复杂度 O(n)',1),('t4','图的深度优先遍历_测试',20,2,'图,DFS','LeetCode','2025-07-04 13:00:00','2025-07-08 18:00:00','给定一个图，使用深度优先搜索（DFS）遍历图中的所有节点。',0.9,1,'使用栈来模拟递归，DFS遍历优化',1),('t5','最小生成树_测试',18,3,'图,生成树','LeetCode','2025-07-05 14:00:00','2025-07-09 19:00:00','给定一个图，求最小生成树的权值总和。',0.5,1,'使用Kruskal算法，时间复杂度 O(E log E)',1),('t6','广度优先遍历_测试',25,4,'图,BFS','LeetCode','2025-07-06 15:00:00','2025-07-10 20:00:00','给定一个图，使用广度优先搜索（BFS）遍历图中的所有节点。',0.7,1,'使用队列来模拟BFS，BFS遍历优化',1),('t7','二分查找_测试',30,1,'数组,查找','LeetCode','2025-07-07 16:00:00','2025-07-11 21:00:00','给定一个排序的数组 nums 和一个目标值 target，判断目标值是否存在于数组中。',0.8,1,'使用二分查找算法，时间复杂度 O(log n)',1),('t8','背包问题_测试',10,2,'动态规划,背包','LeetCode','2025-07-08 17:00:00','2025-07-12 22:00:00','给定一个背包容量和若干物品，求背包能装下的最大价值。',0.6,1,'使用动态规划，空间复杂度 O(n)',1),('t9','排序算法_测试',22,4,'排序,算法','LeetCode','2025-07-09 18:00:00','2025-07-13 23:00:00','给定一个整数数组 nums，排序并返回排序后的数组。',0.9,1,'使用快速排序，时间复杂度 O(n log n)',1),('test','测试',3,0,'test','test','2025-07-19 20:40:31','2025-07-19 20:40:34','test',0,1,'test',1);
/*!40000 ALTER TABLE `problem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `record`
--

DROP TABLE IF EXISTS `record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `record` (
  `userName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cnname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `problemName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `timeUsed` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `memoryUsed` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `submitTime` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `record`
--

LOCK TABLES `record` WRITE;
/*!40000 ALTER TABLE `record` DISABLE KEYS */;
/*!40000 ALTER TABLE `record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_name` varchar(30) NOT NULL,
  `cnname` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(30) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('aaa','','$2a$10$D84cgD5p74uC8bzpr50stO3Nyl5CnsFcnb/wXG45XkZ7uPSEubPym','ROOT',1),('aaaaa','','AAAaaa1.','USER',0),('aaab','','aaa','USER',1);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-04  2:08:42
