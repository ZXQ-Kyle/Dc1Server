# Dc1Server
Dc1Server

项目Fork自ZXQ-Kyle/Dc1Server

DC1插座复活的服务器端

修改了如下：
1.查询间隔由1分钟改为5秒,以便于实时观察功率变化,会增加网络传输
2.将ConnectionManager中的mDeviceConnectionMap的key由ip改为ip+port,以解决将server部署到云服务器,同一路由器下的DC1 ip相同,mDeviceConnectionMap覆盖重复查询问题
3.新增心跳检测,15秒未通讯关闭channel
4.关闭channel时,将DC1 online置为false,以便客户端显示离线状态

感谢原作者.
