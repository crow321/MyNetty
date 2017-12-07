package cn.zhp.netty.others;

import cn.qtec.qkcl.key_store.entity.NodeEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhangp on 2017/6/7.
 */
@Component
public class NodeStatusBuffer {
    private List<NodeEntity> allNodeStatusList = new ArrayList<>();
    //记录数据库中节点总数
    private int nodeStatusNumber;
    //节点状态信息自动更新频率 5 分钟
    private final long AUTO_UPDATE_TIME = 1 * 60 * 1000;
    //节点状态信息自动更新频率 2 小时
    private final long REMOVE_FROM_BUFFER_TIME = 2 * 60 * 1000;

    public void addNodeStatusToBuffer(NodeEntity entity) {
        long userID = entity.getUserID();
        int count = 0;

        //若缓存中存在该节点，则更新该节点状态信息
        for (int i = 0; i < allNodeStatusList.size(); i++) {
            if (userID == allNodeStatusList.get(i).getUserID()) {
                allNodeStatusList.set(i, entity);
                break;
            }
            count++;
        }

        //如果缓存中没有该节点状态信息，则添加到缓存数据
        if (allNodeStatusList.size() == count) {
            allNodeStatusList.add(entity);
        }
    }

    /**
     * 自动更新所有缓存节点信息
     */
    private void updateAllUnKnownNodeStatus() {
        for (NodeEntity entity : allNodeStatusList) {
            long timeDifference = System.currentTimeMillis() - entity.getCreateTime();
            if (timeDifference > AUTO_UPDATE_TIME) {
                entity.setKeyUsedRate(0);
                entity.setKeyGenerateRate(0);
                entity.setKeyUsedRate(0);
                entity.setRawKeyRate(0);
                entity.setOperationStatus(0);
                entity.setUserNumber(0);
            }
        }
    }

    /**
     * 若同步时间距离当前时间在 2小时以外，则将该节点从缓存中去除
     * 若同步时间距离当前时间在 5min ~ 2h 之间，则将该节点部分数据更新
     */
    private void updateUnknownNodeStatus(NodeEntity entity) {
        long timeDifference = System.currentTimeMillis() - entity.getCreateTime();
        if (timeDifference > REMOVE_FROM_BUFFER_TIME) {
            removeNodeStatusFromBuffer(entity.getUserID());
        } else {
            if (timeDifference > AUTO_UPDATE_TIME) {
                entity.setKeyUsedRate(0);
                entity.setKeyGenerateRate(0);
                entity.setKeyUsedRate(0);
                entity.setRawKeyRate(0);
                entity.setOperationStatus(0);
                entity.setUserNumber(0);
            }
        }
    }

    public void removeNodeStatusFromBuffer(long userID) {
        Iterator<NodeEntity> iterator = allNodeStatusList.iterator();
        while (iterator.hasNext()) {
            if (userID == iterator.next().getUserID()) {
                iterator.remove();
                nodeStatusNumber--;
            }
        }
    }

    public List<NodeEntity> getAllNodeStatusList() {
        updateAllUnKnownNodeStatus();
        return allNodeStatusList;
    }

    public void setAllNodeStatusList(List<NodeEntity> allNodeStatusList) {
        this.allNodeStatusList = allNodeStatusList;
    }

    public int getNodeStatusNumber() {
        return nodeStatusNumber;
    }

    public void setNodeStatusNumber(int nodeStatusNumber) {
        this.nodeStatusNumber = nodeStatusNumber;
    }

    public NodeEntity getNodeEntityByUserID(long userID) {
        for (NodeEntity node : allNodeStatusList) {
            if (userID == node.getUserID()) {
                updateUnknownNodeStatus(node);
                return node;
            }
        }
        return null;
    }
}
