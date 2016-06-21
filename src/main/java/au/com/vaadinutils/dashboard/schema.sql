CREATE TABLE `tblportallayout` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  `default` tinyint(4) NOT NULL DEFAULT '0',
  `agentId` int(11) NOT NULL,
  `guid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;


CREATE TABLE `tblportal` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(80) NOT NULL,
  `layoutId` int(11) NOT NULL,
  `guid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

CREATE TABLE `tblportalconfig` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `portalId` int(11) NOT NULL,
  `key` varchar(245) DEFAULT NULL,
  `value` varchar(600) NOT NULL,
  `guid` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;