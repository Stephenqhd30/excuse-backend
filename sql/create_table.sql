# 数据库初始化
# @author stephen qiu
#

-- 创建库
create database if not exists excuse;

-- 切换库
use excuse;

-- 用户表
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userGender   tinyint      default 2                 not null comment '性别（0-男，1-女，2-保密）',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userPhone    varchar(256)                           null comment '手机号码',
    userEmail    varchar(256)                           null comment '用户邮箱',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    editTime     datetime     default CURRENT_TIMESTAMP null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_unionId
    on user (unionId);

-- 标签表
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       not null comment '标签名称',
    userId     bigint                             not null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint  default 0                 null comment '0-不是父标签，1-是父标签',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '标签表';

-- 文件上传日志记录表
create table log_files
(
    id               bigint auto_increment comment 'id'
        primary key,
    fileKey          varchar(255)                        not null comment '文件唯一摘要值',
    fileName         varchar(255)                        not null comment '文件存储名称',
    fileOriginalName varchar(255)                        not null comment '文件原名称',
    fileSuffix       varchar(255)                        not null comment '文件扩展名',
    fileSize         bigint                              not null comment '文件大小',
    fileUrl          varchar(255)                        not null comment '文件地址',
    fileOssType      varchar(20)                         not null comment '文件OSS类型',
    createTime       datetime  default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint   default 0                 not null comment '逻辑删除（0表示未删除，1表示已删除）',
    constraint log_files_pk
        unique (fileKey)
)
    comment '文件上传日志记录表' collate = utf8mb4_general_ci
                                 row_format = DYNAMIC;

