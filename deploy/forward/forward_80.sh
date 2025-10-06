#!/bin/bash
# 脚本名称: forward_80.sh
# 功能: 将本地 80 端口请求转发到指定的远程 IP 的 80 端口。

# ==========================================================
# 检查与变量定义
# ==========================================================

# 检查是否以 root 权限运行
if [[ $EUID -ne 0 ]]; then
   echo "此脚本必须使用 sudo 或 root 权限运行。"
   exit 1
fi

# 检查是否提供了目标 IP 参数
if [ -z "$1" ]; then
    echo "------------------------------------------------"
    echo "错误: 未提供目标服务器 IP 地址。"
    echo "使用方法: sudo $0 <目标服务器IP>"
    echo "示例: sudo $0 192.168.1.100"
    echo "------------------------------------------------"
    exit 1
fi

# 定义变量
TARGET_IP="$1"
SOURCE_PORT="80"
TARGET_PORT="80"
NAT_TABLE="nat"

echo "=========================================="
echo "开始配置端口转发:"
echo "源端口 (本机): TCP/$SOURCE_PORT"
echo "目标 IP: $TARGET_IP"
echo "目标端口: TCP/$TARGET_PORT"
echo "=========================================="


# ==========================================================
# 1. 启用 IP 转发
# ==========================================================

echo "[1/3] 正在启用 IP 转发..."
# 永久设置 /etc/sysctl.conf
if ! grep -q "net.ipv4.ip_forward = 1" /etc/sysctl.conf; then
    echo "net.ipv4.ip_forward = 1" >> /etc/sysctl.conf
    sysctl -p # 立即生效
    echo "IP 转发已永久开启。"
else
    sysctl -w net.ipv4.ip_forward=1 > /dev/null # 确保已生效
    echo "IP 转发已启用。"
fi


# ==========================================================
# 2. 配置 iptables DNAT 规则
# ==========================================================

echo "[2/3] 正在配置 iptables DNAT 规则..."

# [可选] 先尝试清除完全相同的旧规则，避免重复
iptables -t $NAT_TABLE -D PREROUTING -p tcp --dport $SOURCE_PORT -j DNAT --to-destination $TARGET_IP:$TARGET_PORT 2>/dev/null

# 添加新的 DNAT 规则
iptables -t $NAT_TABLE -A PREROUTING -p tcp --dport $SOURCE_PORT -j DNAT --to-destination $TARGET_IP:$TARGET_PORT

if [ $? -eq 0 ]; then
    echo "DNAT 规则添加成功。"
else
    echo "错误: DNAT 规则添加失败。请检查 iptables 组件。"
    exit 1
fi


# ==========================================================
# 3. 保存规则
# ==========================================================

echo "[3/3] 正在保存 iptables 规则以防止重启丢失..."

# 检查并使用 netfilter-persistent 来保存规则
if command -v netfilter-persistent &> /dev/null; then
    netfilter-persistent save
    echo "规则已通过 netfilter-persistent 永久保存。"
else
    echo "警告: 未检测到 netfilter-persistent。请先安装 (sudo apt install iptables-persistent)，否则规则将在重启后丢失！"
fi

echo "=========================================="
echo "转发配置完成！"
echo "请使用以下命令确认规则:"
echo "sudo iptables -t nat -L -n -v | grep $TARGET_IP"
echo "=========================================="