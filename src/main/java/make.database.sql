-- --------------------------------------------------------
-- Host:                         localhost
-- Server version:               5.5.24 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2012-12-16 16:03:40
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS=0;

-- Dumping database structure for aid
CREATE DATABASE IF NOT EXISTS `aid` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `aid`;


-- Dumping structure for table aid.block
CREATE TABLE IF NOT EXISTS `block` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='Blocked Items. Programm will tag files.';

-- Dumping structure for table aid.cache
CREATE TABLE IF NOT EXISTS `cache` (
  `id` varchar(48) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Recently downloaded files';

-- Dumping structure for table aid.dnw
CREATE TABLE IF NOT EXISTS `dnw` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='Unwanted files';

-- Dumping structure for table aid.fileindex
CREATE TABLE IF NOT EXISTS `fileindex` (
  `id` varchar(64) CHARACTER SET ascii NOT NULL,
  `size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `pathfragments` text CHARACTER SET ascii NOT NULL,
  `location` smallint(5) unsigned NOT NULL,
  `isduplicate` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`),
  KEY `index_location_FK` (`location`),
  CONSTRAINT `index_location_FK` FOREIGN KEY (`location`) REFERENCES `location_tags` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='all known files';


-- Dumping structure for table aid.filter
CREATE TABLE IF NOT EXISTS `filter` (
  `id` varchar(50) NOT NULL COMMENT 'full url',
  `board` varchar(2) NOT NULL COMMENT 'board',
  `reason` varchar(30) NOT NULL COMMENT 'filter description',
  `status` tinyint(3) unsigned NOT NULL COMMENT 'filter status',
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='filtered threads';


-- Dumping structure for procedure aid.ListCompare
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `ListCompare`()
    READS SQL DATA
    SQL SECURITY INVOKER
    COMMENT 'Compare the index with block and dnw tables'
BEGIN
select indexview.id, indexview.fullpath, indexview.location from indexview join dnw on indexview.id=dnw.id;
select indexview.id, indexview.fullpath, indexview.location from indexview join block on indexview.id=block.id;
END//
DELIMITER ;


-- Dumping structure for table aid.location_tags
CREATE TABLE IF NOT EXISTS `location_tags` (
  `tag_id` smallint(8) unsigned NOT NULL AUTO_INCREMENT,
  `location` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `tag_id` (`tag_id`),
  UNIQUE KEY `location` (`location`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='List of tags for different locations where data is stored';

-- Dumping data for table aid.location_tags: ~2 rows (approximately)
/*!40000 ALTER TABLE `location_tags` DISABLE KEYS */;
INSERT INTO `location_tags` (`tag_id`, `location`) VALUES
	(2, 'ARCHIVE'),
	(1, 'UNKNOWN');
/*!40000 ALTER TABLE `location_tags` ENABLE KEYS */;


-- Dumping structure for table aid.pathfragment
CREATE TABLE IF NOT EXISTS `pathfragment` (
  `id` mediumint(10) unsigned NOT NULL AUTO_INCREMENT,
  `fragment` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `fragment_uniqe` (`fragment`),
  KEY `fragment_index` (`fragment`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Contains elements used to construct paths to files';

-- Dumping data for table aid.pathfragment: ~0 rows (approximately)
/*!40000 ALTER TABLE `pathfragment` DISABLE KEYS */;
/*!40000 ALTER TABLE `pathfragment` ENABLE KEYS */;


-- Dumping structure for table aid.settings
CREATE TABLE IF NOT EXISTS `settings` (
  `name` varchar(20) NOT NULL,
  `param` text NOT NULL,
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='Global settings for all clients';

-- Dumping data for table aid.settings: 1 rows
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` (`name`, `param`) VALUES
	('SchemaVersion', '4');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;


-- Dumping structure for procedure aid.StripDriveLetter
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `StripDriveLetter`()
    MODIFIES SQL DATA
    SQL SECURITY INVOKER
BEGIN
DECLARE done INT DEFAULT 0;
  DECLARE i MEDIUMINT DEFAULT 0; 
  DECLARE d VARCHAR(255) character set utf8;
  DECLARE cur1 CURSOR FOR SELECT dirlist.id, SUBSTR(dirlist.dirpath FROM 3 FOR (CHAR_LENGTH(dirlist.dirpath))) FROM dirlist WHERE dirpath LIKE '_:%';

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
  SET unique_checks=0;
  SET foreign_key_checks=0;

  START TRANSACTION;
  OPEN cur1;

  read_loop: LOOP
    FETCH cur1 INTO i,d;

    IF done THEN
      LEAVE read_loop;
    END IF;
    
    UPDATE dirlist SET dirlist.dirpath = d WHERE dirlist.id = i;
  END LOOP;

  CLOSE cur1;
  COMMIT;
  
  SET unique_checks=1;
  SET foreign_key_checks=1;
END//
DELIMITER ;


-- Dumping structure for table aid.thumbs
CREATE TABLE IF NOT EXISTS `thumbs` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `url` varchar(50) CHARACTER SET ascii NOT NULL,
  `filename` varchar(25) CHARACTER SET ascii NOT NULL,
  `thumb` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Thumbnails for items in the Filter list';

-- Dumping structure for procedure aid.WarmUpDB
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `WarmUpDB`()
    READS SQL DATA
    SQL SECURITY INVOKER
    COMMENT 'Load all data in to memory (make sure there is enough memory available)'
BEGIN
	SELECT * FROM block;
	SELECT * FROM  `cache`;
	SELECT * FROM dirlist;
	SELECT * FROM dnw;
	SELECT * FROM fileduplicate;
	SELECT * FROM fileindex;
	SELECT * FROM filelist;
	SELECT * FROM filter;
	SELECT * FROM location_tags;
	SELECT * FROM settings;
	SELECT * FROM thumbs;
END//
DELIMITER ;


-- Dumping structure for trigger aid.prune_thumbs_del
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `prune_thumbs_del` AFTER DELETE ON `filter` FOR EACH ROW BEGIN
	DELETE FROM thumbs WHERE url = OLD.id;
END//
DELIMITER ;
SET SQL_MODE=@OLD_SQL_MODE;


-- Dumping structure for trigger aid.prune_thumbs_up
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `prune_thumbs_up` AFTER UPDATE ON `filter` FOR EACH ROW BEGIN
	IF NEW.status != 1 THEN
		DELETE FROM thumbs WHERE url = OLD.id;
	END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=1;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
