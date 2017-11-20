package com.hzdz.ls.service;

import com.hzdz.ls.common.FileUtil;
import com.hzdz.ls.common.Result;
import com.hzdz.ls.common.ResultDetail;
import com.hzdz.ls.common.StringUtil;
import com.hzdz.ls.db.entity.SystemActivity;
import com.hzdz.ls.db.entity.SystemActivityModuleMap;
import com.hzdz.ls.db.entity.SystemManager;
import com.hzdz.ls.db.impl.SystemActivityMapper;
import com.hzdz.ls.db.impl.SystemActivityModuleMapMapper;
import com.hzdz.ls.intercepter.MyIntercepter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(rollbackForClassName = "Exception")
public class SystemActivityServer {

    @Autowired
    private SystemActivityMapper systemActivityMapper;

    @Autowired
    private SystemActivityModuleMapMapper systemActivityModuleMapMapper;


    public Result addNewActivity(String activityName, Integer belongManager, Integer templateId, MultipartFile shareImage, String shareText, Integer[] moduleIds, HttpServletRequest request) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        boolean roollerBackFlag = false;
        // 判断上传文件是否为空
        if (shareImage.isEmpty()) {
            data.put("code", -1);
            data.put("msg", "图片上传失败！");
        }else {
            // 获取当前项目根路径
            String path = request.getSession().getServletContext().getRealPath("/");
            // 创建活动对象，进行插入操作，获取新增后的主键ID
            SystemActivity systemActivity = new SystemActivity();
            systemActivity.setActivityName(activityName);
            systemActivity.setAddTime(new Date(System.currentTimeMillis()));
            systemActivity.setBelongManager(belongManager);
            systemActivity.setShareImage(path);
            systemActivity.setShareText(shareText);
            systemActivity.setUpdateTime(new Date(System.currentTimeMillis()));
            systemActivity.setTemplateId(templateId);
            if (systemActivityMapper.addNewActivity(systemActivity) < 1) {
                data.put("code", -1);
                data.put("msg", "创建活动失败！");
                roollerBackFlag = true;
            }else {
                // 新增后主键ID作为文件夹名
                int systemActivityId = systemActivity.getId();
                String imagePath = "upload/" + belongManager + "/" + systemActivityId;
                // 进行文件上传操作
                String fileUrl = FileUtil.upload4Stream(shareImage.getInputStream(), path + imagePath, shareImage.getOriginalFilename());
                if (!StringUtil.checkEmpty(fileUrl)) {
                    data.put("code", -1);
                    data.put("msg", "图片上传失败！");
                    roollerBackFlag = true;
                }else {
                    // 更新图片路径
                    systemActivity.setId(systemActivityId);
                    systemActivity.setShareImage(imagePath + "/" + fileUrl);
                    if (systemActivityMapper.updateShareImage(systemActivity) < 1) {
                        data.put("code", -1);
                        data.put("msg", "创建活动失败！");
                        roollerBackFlag = true;
                    } else {
                        // 新增活动与模版间的映射
                        SystemActivityModuleMap systemActivityModuleMap = null;
                        boolean setMapFlag = false;
                        // 遍历模版数组ID
                        for (int moduleId : moduleIds){
                            systemActivityModuleMap = new SystemActivityModuleMap();
                            systemActivityModuleMap.setActivityId(systemActivityId);
                            systemActivityModuleMap.setModuleId(moduleId);
                            systemActivityModuleMap.setAddTime(new Date(System.currentTimeMillis()));
                            if(systemActivityModuleMapMapper.addNewMap(systemActivityModuleMap) < 1){
                                setMapFlag = true;
                                break;
                            }
                        }
                        if(setMapFlag){
                            data.put("code", -1);
                            data.put("msg", "添加活动模块失败！");
                            roollerBackFlag = true;
                        }else {
                            data.put("code", 0);
                            data.put("msg", "创建活动成功！");
                        }
                    }
                }
            }
        }
        //判断是否回滚事务
        if (roollerBackFlag){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return new ResultDetail(data);
    }

    @Transactional(rollbackForClassName = "Exception")
    public Result deleteActivity(Integer activityId, HttpServletRequest request){
        Map<String, Object> data = new HashMap<String, Object>();
        boolean roollerBackFlag = false;
        SystemManager systemManager = MyIntercepter.getManager(request);
        SystemActivity systemActivity = systemActivityMapper.selectActivityById(activityId);
        int belongId = systemActivity.getBelongManager();
        if(systemManager.getManagerType() == 1 || belongId == systemManager.getId()){
            if(systemActivityMapper.deleteActivity(activityId) < 1){
                data.put("code", -1);
                data.put("msg", "删除活动失败！");
            }else{
                if(systemActivityModuleMapMapper.deleteActivityById(activityId) < 1){
                    roollerBackFlag = true;
                    data.put("code", -1);
                    data.put("msg", "删除活动失败！");
                }else {
                    data.put("code", 0);
                    data.put("msg", "删除活动成功！");
                }
            }
        }else{
            data.put("code", -1);
            data.put("msg", "删除活动失败，非超管不能删除不属于自己的活动！");
        }
        //判断是否回滚事务
        if (roollerBackFlag){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return new ResultDetail(data);
    }

    public Result updateShareImage(Integer activityId, MultipartFile shareImage, String shareText, HttpServletRequest request) throws IOException{
        Map<String, Object> data = new HashMap<>();
        if (shareImage.isEmpty()) {
            data.put("code", -1);
            data.put("msg", "图片上传失败！");
        }else {
            SystemActivity systemActivity = systemActivityMapper.selectActivityById(activityId);
            Integer belongManager = systemActivity.getBelongManager();
            String customaryPath = systemActivity.getShareImage();
            String originalPath = request.getSession().getServletContext().getRealPath("/");
            String imagePath = "upload/" + belongManager + "/" + activityId;
            // 进行文件上传操作
            String fileUrl = FileUtil.upload4Stream(shareImage.getInputStream(), originalPath + imagePath, shareImage.getOriginalFilename());
            if (!StringUtil.checkEmpty(fileUrl)) {
                data.put("code", -1);
                data.put("msg", "图片上传失败！");
            }else {
                systemActivity.setShareImage(imagePath + "/" + fileUrl);
                systemActivity.setShareText(shareText);
                systemActivity.setUpdateTime(new Date(System.currentTimeMillis()));
                if (systemActivityMapper.updateShareImage(systemActivity) < 1) {
                    data.put("code", -1);
                    data.put("msg", "更新图文信息失败！");
                } else {
                    FileUtil.delete(originalPath+customaryPath);
                    data.put("code", 0);
                    data.put("msg", "更新图文信息成功！");
                }
            }
        }
        return new ResultDetail(data);
    }

}