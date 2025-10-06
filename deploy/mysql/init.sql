-- 创建远程 root 用户
CREATE USER 'letuc'@'%' IDENTIFIED BY '430103535';

-- 授予所有权限给 root 用户
GRANT ALL PRIVILEGES ON *.* TO 'letuc'@'%' WITH GRANT OPTION;

-- 刷新权限
FLUSH PRIVILEGES;
