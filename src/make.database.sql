-- --------------------------------------------------------
-- Host:                         localhost
-- Server version:               5.5.24 - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL version:             7.0.0.4053
-- Date/time:                    2012-07-06 15:05:54
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;

-- Dumping database structure for file_hash
CREATE DATABASE IF NOT EXISTS `file_hash` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `file_hash`;


-- Dumping structure for table file_hash.block
CREATE TABLE IF NOT EXISTS `block` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='blocked Items. Programm will tag folders.';

-- Data exporting was unselected.


-- Dumping structure for table file_hash.cache
CREATE TABLE IF NOT EXISTS `cache` (
  `id` varchar(48) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='recently downloaded files';

-- Data exporting was unselected.


-- Dumping structure for table file_hash.dirlist
CREATE TABLE IF NOT EXISTS `dirlist` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `dirpath` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `dirpath` (`dirpath`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table file_hash.dnw
CREATE TABLE IF NOT EXISTS `dnw` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hash` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='unwanted files';

-- Data exporting was unselected.


-- Dumping structure for view file_hash.dupeview
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `dupeview` (
	`id` VARCHAR(64) NOT NULL COLLATE 'ascii_general_ci',
	`dupePath` VARCHAR(510) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
	`origPath` VARCHAR(510) NULL DEFAULT NULL COLLATE 'utf8_general_ci'
) ENGINE=MyISAM;


-- Dumping structure for table file_hash.duplicate
CREATE TABLE IF NOT EXISTS `duplicate` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='all known files';

-- Data exporting was unselected.


-- Dumping structure for table file_hash.filelist
CREATE TABLE IF NOT EXISTS `filelist` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `filename` (`filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table file_hash.filter
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


-- Dumping structure for table file_hash.index
CREATE TABLE IF NOT EXISTS `index` (
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


-- Dumping structure for view file_hash.indexview
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `indexview` (
	`id` VARCHAR(64) NOT NULL COLLATE 'ascii_general_ci',
	`fullpath` VARCHAR(510) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
	`size` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',
	`location` VARCHAR(30) NOT NULL COLLATE 'utf8_unicode_ci'
) ENGINE=MyISAM;


-- Dumping structure for table file_hash.location_tags
CREATE TABLE IF NOT EXISTS `location_tags` (
  `tag_id` smallint(8) unsigned NOT NULL,
  `location` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `tag_id` (`tag_id`),
  UNIQUE KEY `location` (`location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- Data exporting was unselected.


-- Dumping structure for table file_hash.settings
CREATE TABLE IF NOT EXISTS `settings` (
  `name` varchar(20) NOT NULL,
  `param` text NOT NULL,
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii;

-- Data exporting was unselected.


-- Dumping structure for table file_hash.thumbs
CREATE TABLE IF NOT EXISTS `thumbs` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `url` varchar(50) CHARACTER SET ascii NOT NULL,
  `filename` varchar(25) CHARACTER SET ascii NOT NULL,
  `thumb` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Thumbnails for items in the Filter list';

-- Data exporting was unselected.


-- Dumping structure for view file_hash.dupeview
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `dupeview`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` VIEW `dupeview` AS SELECT `duplicate`.id, CONCAT(dirlist.dirpath, filelist.filename) AS dupePath, CONCAT(idir.dirpath, ifile.filename) AS origPath FROM 
	`duplicate` JOIN `index` ON `duplicate`.id = `index`.id 
		JOIN filelist ON `duplicate`.filename=filelist.id 
			JOIN dirlist ON `duplicate`.dir=dirlist.id 
				JOIN dirlist as idir ON idir.id = `index`.dir 
					JOIN filelist as ifile ON `index`.filename = ifile.id ;


-- Dumping structure for view file_hash.indexview
-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `indexview`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` VIEW `indexview` AS SELECT a.id, CONCAT(dirlist.dirpath,filelist.filename) as fullpath, a.size, location_tags.location FROM `index` as a JOIN filelist ON a.filename=filelist.id JOIN dirlist on a.dir=dirlist.id JOIN location_tags ON a.location = location_tags.tag_id ;
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
