package com.hzdz.ls.service.module;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hzdz.ls.common.*;
import com.hzdz.ls.db.entity.SystemActivity;
import com.hzdz.ls.db.entity.SystemManager;
import com.hzdz.ls.db.entity.module.CloudPhotography;
import com.hzdz.ls.db.impl.SystemActivityMapper;
import com.hzdz.ls.db.impl.module.CloudPhotographyMapper;
import com.hzdz.ls.db.pagehelper.PageContent;
import com.hzdz.ls.intercepter.MyIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
*
*@author 豆豆
*时间:
*/
@Service
public class CloudPhotographyServer {

    @Autowired
    private CloudPhotographyMapper cloudPhotographyMapper;

    @Autowired
    private SystemActivityMapper systemActivityMapper;

    public PageContent<CloudPhotography> list(int activityId, int pageNo, int pageSize){
        PageHelper.startPage(pageNo, pageSize);
        Page<CloudPhotography> list = cloudPhotographyMapper.queryAllByActivityId(activityId);
        List<CloudPhotography> volist = new ArrayList<>();
        for (CloudPhotography cp : list){
            //这步暂时看来是多余的，如果后续列表展示需要其他的信息则需要在这里对数据处理里
             volist.add(cp);
        }

        PageContent<CloudPhotography> pageContent = new PageContent<CloudPhotography>(volist, list.getPageNum(),
                list.getPageSize(), list.getTotal(), list.getPages());
        return pageContent;
    }

    @Transactional(rollbackForClassName = "Exception")
    public Result multiUploadImage(MultipartFile[] files, Integer activityId, HttpServletRequest request) throws Exception{
        Map<String, Object> data = new HashMap<>();
        String CONTEXT_PATH = request.getSession().getServletContext().getRealPath("/");
        SystemActivity systemActivity = systemActivityMapper.selectActivityById(activityId);
        Integer managerId = MyIntercepter.getManagerId(request);
        //String activityName = systemActivity.getActivityName();
        if (systemActivity != null){
            //二维码名称，使用最后一个文件的名字
            String codeName = "";
            int n = files.length;
            List<String> imageList = new ArrayList<>();
            if (n < 1){
                data.put("code", -1);
                data.put("msg", "空文件！");
            }else {
                for (int i = 0; i < n; i++) {
                    MultipartFile file = files[i];
                    codeName = FileUtil.upload4Stream(file.getInputStream(),
                            CONTEXT_PATH + "upload/manager/"+managerId+"/"+activityId+"/1",
                            file.getOriginalFilename());
                    if (!StringUtil.checkEmpty(codeName)) {
                        data.put("code", -1);
                        data.put("msg", "照片上传失败！");
                        return new ResultDetail<>(data);
                    }else {
                        //将图片名称存起来
                        imageList.add("/upload/manager/"+managerId+"/" + activityId +"/1" + "/" + codeName);
                    }
                }
                String codeUrl = "upload/manager/"+ managerId + "/" + activityId + "/1" + "/code/" + QRcodeUtil.encode(BaseVar.BASE_URL+"/index.html?activityId="+activityId,
                        "", CONTEXT_PATH + "upload/manager/" + managerId + "/" + activityId + "/1" +"/code/", codeName, true);
                systemActivity.setQRCode(codeUrl);
                if(systemActivityMapper.updateQrCode(systemActivity) < 1){
                    data.put("code", -1);
                    data.put("msg", "照片上传失败！");
                }else {
                    for (String image : imageList){
                        CloudPhotography cloudPhotography = new CloudPhotography();
                        cloudPhotography.setActivityId(activityId);
                        cloudPhotography.setImageUrl(image);
                        cloudPhotography.setAddTime(new Date(System.currentTimeMillis()));
                        if (cloudPhotographyMapper.addCloudPhotography(cloudPhotography) < 1){
                            data.put("code", -1);
                            data.put("msg", "照片上传失败！");
                            return new ResultDetail<>(data);
                        }
                    }
                    data.put("code", 0);
                    data.put("msg", "照片上传成功！");
                }
            }
        } else {
            data.put("code", -1);
            data.put("msg", "该活动不存在！");
        }
        return new ResultDetail<>(data);
    }
}
