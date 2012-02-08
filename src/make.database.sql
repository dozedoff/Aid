/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE DATABASE IF NOT EXISTS `aid` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;
USE `aid`;

CREATE TABLE IF NOT EXISTS `settings` (
	`name` varchar(20) NOT NULL,
	`param` TEXT NOT NULL,
	UNIQUE INDEX `name` (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii;

INSERT IGNORE INTO settings (name,param) VALUES ('SchemaVersion', '2');

CREATE TABLE IF NOT EXISTS `archive` (
  `id` varchar(64) CHARACTER SET ascii NOT NULL,
  `size` bigint(20) NOT NULL,
  `dir` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `filename` mediumint(8) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`hash`),
  UNIQUE KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='files in storage';

CREATE TABLE IF NOT EXISTS `block` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`hash`),
  UNIQUE KEY `hash` (`hash`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='blocked Items. Programm will tag files.';

CREATE TABLE IF NOT EXISTS `cache` (
  `id` varchar(48) NOT NULL,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='recently downloaded files';

CREATE TABLE IF NOT EXISTS `dirlist` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `dirpath` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `dirpath` (`dirpath`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `dnw` (
  `id` varchar(64) NOT NULL,
  PRIMARY KEY (`hash`),
  UNIQUE KEY `hash` (`hash`)
) ENGINE=MyISAM DEFAULT CHARSET=ascii COMMENT='unwanted files';

CREATE TABLE IF NOT EXISTS `filelist` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `filename` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `filename` (`filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `filter` (
  `id` varchar(50) NOT NULL COMMENT 'full url',
  `board` varchar(2) NOT NULL COMMENT 'board',
  `reason` varchar(30) NOT NULL COMMENT 'filter description',
  `status` tinyint(3) unsigned NOT NULL COMMENT 'filter status',
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='filtered threads';

CREATE TABLE IF NOT EXISTS `hash` (
  `id` varchar(64) CHARACTER SET ascii NOT NULL,
  `size` bigint(20) NOT NULL,
  `dir` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `filename` mediumint(8) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`hash`),
  UNIQUE KEY `hash` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='all known files';

CREATE TABLE IF NOT EXISTS `thumbs` (
  `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `url` varchar(50) CHARACTER SET ascii NOT NULL,
  `filename` varchar(25) CHARACTER SET ascii NOT NULL,
  `thumb` blob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Thumbnails for items in the filter list';

SET SESSION SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `prune_thumbs_del` AFTER DELETE ON `filter` FOR EACH ROW BEGIN
	DELETE FROM thumbs WHERE url = OLD.id;
END//
DELIMITER ;
SET SESSION SQL_MODE=@OLD_SQL_MODE;

SET SESSION SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `prune_thumbs_up` AFTER UPDATE ON `filter` FOR EACH ROW BEGIN
	IF NEW.status != 1 THEN
		DELETE FROM thumbs WHERE url = OLD.id;
	END IF;
END//
DELIMITER ;
SET SESSION SQL_MODE=@OLD_SQL_MODE;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
