/*
Navicat MySQL Data Transfer

Source Server         : 192.168.90.233
Source Server Version : 50554
Source Host           : 192.168.90.233:3306
Source Database       : QkPool

Target Server Type    : MYSQL
Target Server Version : 50554
File Encoding         : 65001

Date: 2017-12-25 10:26:18
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for qkey_authentication_info
-- ----------------------------
DROP TABLE IF EXISTS `qkey_authentication_info`;
CREATE TABLE `qkey_authentication_info` (
  `user_id` int(11) NOT NULL,
  `user_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'Q盾deviceid',
  `password` varbinary(128) NOT NULL COMMENT '客户端加密报文密钥值',
  `salt` varbinary(128) NOT NULL COMMENT '随机盐值',
  `passType` smallint(6) DEFAULT '2' COMMENT '1: 密码来源于web界面 2：密码来源于量子随机数',
  `iteration_count` smallint(16) unsigned NOT NULL COMMENT '计算轮数',
  `user_guid` varchar(32) DEFAULT NULL,
  `parent_name` varchar(20) DEFAULT NULL,
  `config_version` varchar(20) DEFAULT NULL,
  `root_key_value` varbinary(48) NOT NULL COMMENT '加密报文密钥值',
  `mobilephone` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'Q盾持有人电话',
  `email` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'Q盾持有人邮箱',
  `root_key_id` binary(16) NOT NULL COMMENT '加密报文密钥ID',
  `user_type` smallint(10) unsigned NOT NULL COMMENT '2-普通用户 3-Q盾 4-企业用户',
  `rfu1` varchar(128) DEFAULT '0',
  `rfu2` smallint(10) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`user_name`),
  UNIQUE KEY `root_key_id` (`root_key_id`) USING BTREE,
  KEY `user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
