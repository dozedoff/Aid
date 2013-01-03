-- --------------------------------------------------------
-- Host:                         localhost
-- Server version:               5.5.24 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2012-08-08 17:48:51
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

-- Data exporting was unselected.


-- Dumping structure for table aid.cache
CREATE TABLE IF NOT EXISTS `cache` (
  `id` varchar(48) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='Recently downloaded files';

-- Data exporting was unselected.


-- Dumping structure for table aid.dirlist
CREATE TABLE IF NOT EXISTS `dirlist` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `dirpath` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `dirpath` (`dirpath`),
  UNIQUE KEY `dirpath_unique` (`dirpath`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='List of all known directories';

-- Data exporting was unselected.


-- Dumping structure for table aid.dnw
CREATE TABLE IF NOT EXISTS `dnw` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='Unwanted files';

-- Data exporting was unselected.


-- Dumping structure for view aid.dupeview
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `dupeview` (
	`id` VARCHAR(64) NOT NULL COLLATE 'ascii_general_ci',
	`dupeloc` VARCHAR(30) NOT NULL COLLATE 'utf8_unicode_ci',
	`dupePath` VARCHAR(510) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
	`origloc` VARCHAR(30) NOT NULL COLLATE 'utf8_unicode_ci',
	`origPath` VARCHAR(510) NULL DEFAULT NULL COLLATE 'utf8_general_ci'
) ENGINE=MyISAM;


-- Dumping structure for table aid.fileduplicate
CREATE TABLE IF NOT EXISTS `fileduplicate` (
  `id` varchar(64) CHARACTER SET ascii NOT NULL,
  `size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `dir` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `filename` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `location` smallint(5) unsigned NOT NULL,
  PRIMARY KEY (`id`,`dir`,`filename`),
  UNIQUE KEY `id_dir_filename` (`id`,`dir`,`filename`),
  KEY `location_FK` (`location`),
  KEY `dir_FK` (`dir`),
  KEY `file_fk` (`filename`),
  CONSTRAINT `dup_dir_FK` FOREIGN KEY (`dir`) REFERENCES `dirlist` (`id`) ON DELETE CASCADE,
  CONSTRAINT `dup_file_fk` FOREIGN KEY (`filename`) REFERENCES `filelist` (`id`) ON DELETE CASCADE,
  CONSTRAINT `dup_location_FK` FOREIGN KEY (`location`) REFERENCES `location_tags` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='List of duplicate files';

-- Data exporting was unselected.


-- Dumping structure for table aid.fileindex
CREATE TABLE IF NOT EXISTS `fileindex` (
  `id` varchar(64) CHARACTER SET ascii NOT NULL,
  `size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `dir` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `filename` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `location` smallint(5) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`),
  KEY `index_dir_FK` (`dir`),
  KEY `index_file_fk` (`filename`),
  KEY `index_location_FK` (`location`),
  CONSTRAINT `index_dir_FK` FOREIGN KEY (`dir`) REFERENCES `dirlist` (`id`) ON DELETE CASCADE,
  CONSTRAINT `index_file_fk` FOREIGN KEY (`filename`) REFERENCES `filelist` (`id`) ON DELETE CASCADE,
  CONSTRAINT `index_location_FK` FOREIGN KEY (`location`) REFERENCES `location_tags` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='all known files';

-- Data exporting was unselected.


-- Dumping structure for table aid.filelist
CREATE TABLE IF NOT EXISTS `filelist` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `filename` (`filename`),
  UNIQUE KEY `filename_unique` (`filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='List of all known filenames';

-- Data exporting was unselected.


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

-- Data exporting was unselected.


-- Dumping structure for view aid.indexview
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `indexview` (
	`id` VARCHAR(64) NOT NULL COLLATE 'ascii_general_ci',
	`fullpath` VARCHAR(510) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
	`size` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',
	`location` VARCHAR(30) NOT NULL COLLATE 'utf8_unicode_ci'
) ENGINE=MyISAM;


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='List of tags for different locations where data is stored';

INSERT IGNORE INTO `location_tags` (`tag_id`, `location`) VALUES (1, 'UNKNOWN');
INSERT IGNORE INTO `location_tags` (`tag_id`, `location`) VALUES (2, 'ARCHIVE');

-- Dumping structure for table aid.settings
CREATE TABLE IF NOT EXISTS `settings` (
  `name` varchar(20) NOT NULL,
  `param` text NOT NULL,
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='Global settings for all clients';

INSERT IGNORE INTO `settings` (`name`, `param`) VALUES ('SchemaVersion', '3');



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

-- Data exporting was unselected.
CREATE TABLE `lastmodified` (
	`id` VARCHAR(48) NOT NULL COLLATE 'utf8_unicode_ci',
	`lastmod` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`lastvisit` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`last_mod_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `id` (`id`),
	INDEX `last_mod_id` (`last_mod_id`)
)
COLLATE='utf8_unicode_ci'
ENGINE=InnoDB;


ALTER TABLE `cache`
	ADD COLUMN `last_mod_id` INT UNSIGNED NOT NULL DEFAULT '0' AFTER `timestamp`,
	ADD INDEX `last_mod_id` (`last_mod_id`);
	
ALTER TABLE `cache`
	ADD COLUMN `downloaded` BINARY(1) NOT NULL DEFAULT '0' AFTER `last_mod_id`;

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
	
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `lastmodified_update` AFTER UPDATE ON `lastmodified` FOR EACH ROW BEGIN
	UPDATE `cache` SET `timestamp` = NOW() WHERE `last_mod_id` = NEW.last_mod_id;
END//
DELIMITER ;
SET SQL_MODE=@OLD_SQL_MODE;

SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `lastmodified_delete` AFTER DELETE ON `lastmodified` FOR EACH ROW BEGIN
		DELETE FROM `cache` WHERE last_mod_id = OLD.last_mod_id;
END//
DELIMITER ;
SET SQL_MODE=@OLD_SQL_MODE;

-- Dumping structure for view aid.dupeview
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `dupeview`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` VIEW `dupeview` AS SELECT `fileduplicate`.id, dupeloc.location AS dupeloc, CONCAT(dirlist.dirpath, filelist.filename) AS dupePath, origloc.location AS origloc, CONCAT(idir.dirpath, ifile.filename) AS origPath FROM 
	`fileduplicate` JOIN `fileindex` ON `fileduplicate`.id = `fileindex`.id 
		JOIN filelist ON `fileduplicate`.filename=filelist.id
			JOIN dirlist ON `fileduplicate`.dir=dirlist.id
				JOIN location_tags AS origloc ON origloc.tag_id=fileindex.location
					JOIN dirlist AS idir ON idir.id = `fileindex`.dir 
						JOIN filelist AS ifile ON `fileindex`.filename = ifile.id
							JOIN location_tags AS dupeloc ON dupeloc.tag_id=fileduplicate.location ;


-- Dumping structure for view aid.indexview
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `indexview`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` VIEW `indexview` AS SELECT a.id, CONCAT(dirlist.dirpath,filelist.filename) as fullpath, a.size, location_tags.location FROM `fileindex` as a JOIN filelist ON a.filename=filelist.id JOIN dirlist on a.dir=dirlist.id JOIN location_tags ON a.location = location_tags.tag_id ;
SET FOREIGN_KEY_CHECKS=1;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
