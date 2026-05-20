package com.pcadvisor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pcadvisor.common.Result;
import com.pcadvisor.common.enums.ErrorCode;
import com.pcadvisor.common.exception.BusinessException;
import com.pcadvisor.entity.*;
import com.pcadvisor.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private HardwareInfMapper hardwareInfMapper;

    @Autowired
    private MediaInfMapper mediaInfMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    // ==================== 收藏相关 ====================

    /**
     * 获取当前用户的收藏列表
     */
    @GetMapping("/collections")
    public Result<List<Map<String, Object>>> getCollections() {
        String userId = getCurrentUserId();

        List<Collect> collects = collectMapper.selectList(
            new QueryWrapper<Collect>().eq("user_id", userId)
        );

        if (collects.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<Integer> hardwareIds = collects.stream()
            .map(Collect::getHardwareId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (hardwareIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<HardwareInf> hardwareList = hardwareInfMapper.selectList(
            new QueryWrapper<HardwareInf>().in("HardwareID", hardwareIds)
        );
        Map<Integer, HardwareInf> hardwareMap = hardwareList.stream()
            .collect(Collectors.toMap(HardwareInf::getHardwareId, h -> h));

        List<MediaInf> allMedia = mediaInfMapper.selectList(
            new QueryWrapper<MediaInf>().in("hardware_id", hardwareIds)
        );
        Map<Integer, List<MediaInf>> mediaMap = allMedia.stream()
            .filter(m -> m.getHardwareId() != null)
            .collect(Collectors.groupingBy(MediaInf::getHardwareId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Collect collect : collects) {
            HardwareInf hw = hardwareMap.get(collect.getHardwareId());
            if (hw == null) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("collectId", collect.getCollectId());
            item.put("collectTime", collect.getCollectTime());
            item.put("hardwareId", hw.getHardwareId());
            item.put("hardwareName", hw.getHardwareName());
            item.put("hardwareType", hw.getHardwareType());
            item.put("hardwareBrand", hw.getHardwareBrand());
            item.put("hardwareModel", hw.getHardwareModel());
            item.put("hardwarePrice", hw.getHardwarePrice());

            List<MediaInf> images = mediaMap.get(hw.getHardwareId());
            item.put("imageUrl", (images != null && !images.isEmpty()) ? images.get(0).getMediaUrl() : null);

            result.add(item);
        }

        return Result.success(result);
    }

    /**
     * 收藏硬件
     */
    @PostMapping("/collect")
    public Result<Map<String, Object>> collectHardware(@RequestBody Map<String, Object> body) {
        String userId = getCurrentUserId();
        Integer hardwareId = body.get("hardwareId") != null
            ? ((Number) body.get("hardwareId")).intValue()
            : null;

        if (hardwareId == null) {
            throw new BusinessException("硬件ID不能为空");
        }

        // 检查硬件是否存在
        HardwareInf hw = hardwareInfMapper.selectById(hardwareId);
        if (hw == null) {
            throw new BusinessException(ErrorCode.HARDWARE_NOT_FOUND);
        }

        // 检查是否已收藏
        Collect existing = collectMapper.selectOne(
            new QueryWrapper<Collect>()
                .eq("user_id", userId)
                .eq("hardware_id", hardwareId)
        );

        if (existing != null) {
            // 已收藏则取消收藏
            collectMapper.deleteById(existing.getCollectId());
            Map<String, Object> result = new HashMap<>();
            result.put("collected", false);
            result.put("message", "已取消收藏");
            return Result.success(result);
        }

        // 新建收藏
        Collect collect = new Collect();
        collect.setCollectId("C" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4));
        collect.setUserId(userId);
        collect.setHardwareId(hardwareId);
        collect.setCollectTime(LocalDateTime.now());
        collectMapper.insert(collect);

        Map<String, Object> result = new HashMap<>();
        result.put("collected", true);
        result.put("collectId", collect.getCollectId());
        result.put("message", "收藏成功");
        return Result.success(result);
    }

    /**
     * 检查硬件是否已收藏
     */
    @GetMapping("/collect/check/{hardwareId}")
    public Result<Map<String, Object>> checkCollected(@PathVariable Integer hardwareId) {
        String userId = getCurrentUserId();
        Collect existing = collectMapper.selectOne(
            new QueryWrapper<Collect>()
                .eq("user_id", userId)
                .eq("hardware_id", hardwareId)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("collected", existing != null);
        if (existing != null) {
            result.put("collectId", existing.getCollectId());
        }
        return Result.success(result);
    }

    // ==================== 评测相关 ====================

    /**
     * 获取当前用户发布的评测列表
     */
    @GetMapping("/evaluations")
    public Result<List<Map<String, Object>>> getEvaluations() {
        String userId = getCurrentUserId();

        List<Evaluation> evaluations = evaluationMapper.selectList(
            new QueryWrapper<Evaluation>()
                .eq("user_id", userId)
                .orderByDesc("publish_time")
        );

        if (evaluations.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<Integer> hardwareIds = evaluations.stream()
            .map(Evaluation::getHardwareId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Integer, HardwareInf> hardwareMap = Collections.emptyMap();
        Map<Integer, List<MediaInf>> mediaMap = Collections.emptyMap();

        if (!hardwareIds.isEmpty()) {
            List<HardwareInf> hardwareList = hardwareInfMapper.selectList(
                new QueryWrapper<HardwareInf>().in("HardwareID", hardwareIds)
            );
            hardwareMap = hardwareList.stream()
                .collect(Collectors.toMap(HardwareInf::getHardwareId, h -> h));

            List<MediaInf> allMedia = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("hardware_id", hardwareIds)
            );
            mediaMap = allMedia.stream()
                .filter(m -> m.getHardwareId() != null)
                .collect(Collectors.groupingBy(MediaInf::getHardwareId));
        }

        Set<Integer> evalIds = evaluations.stream()
            .map(Evaluation::getEvaluationId)
            .collect(Collectors.toSet());

        Map<Integer, List<MediaInf>> evalMediaMap = Collections.emptyMap();
        if (!evalIds.isEmpty()) {
            List<MediaInf> evalMedia = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("evaluation_id", evalIds)
            );
            evalMediaMap = evalMedia.stream()
                .filter(m -> m.getEvaluationId() != null)
                .collect(Collectors.groupingBy(MediaInf::getEvaluationId));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Evaluation eval : evaluations) {
            HardwareInf hw = hardwareMap.get(eval.getHardwareId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("evaluationId", eval.getEvaluationId());
            item.put("evaluationTitle", eval.getEvaluationTitle());
            item.put("auditState", eval.getAuditState());
            item.put("publishTime", eval.getPublishTime());
            item.put("perfTestData", eval.getPerfTestData());
            item.put("usageExperience", eval.getUsageExperience());
            item.put("prosAndCons", eval.getProsAndCons());

            if (hw != null) {
                Map<String, Object> hwInfo = new LinkedHashMap<>();
                hwInfo.put("hardwareId", hw.getHardwareId());
                hwInfo.put("hardwareName", hw.getHardwareName());
                hwInfo.put("hardwareType", hw.getHardwareType());
                hwInfo.put("hardwareBrand", hw.getHardwareBrand());
                hwInfo.put("hardwarePrice", hw.getHardwarePrice());

                List<MediaInf> images = mediaMap.get(hw.getHardwareId());
                hwInfo.put("imageUrl", (images != null && !images.isEmpty()) ? images.get(0).getMediaUrl() : null);

                item.put("hardware", hwInfo);
            } else {
                item.put("hardware", null);
            }

            // 评测关联的图片
            List<MediaInf> evalImages = evalMediaMap.getOrDefault(eval.getEvaluationId(), Collections.emptyList());
            item.put("images", evalImages.stream()
                .map(MediaInf::getMediaUrl)
                .collect(Collectors.toList()));

            result.add(item);
        }

        return Result.success(result);
    }

    /**
     * 发布评测
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/evaluate")
    public Result<Map<String, Object>> createEvaluation(@RequestBody Map<String, Object> body) {
        String userId = getCurrentUserId();

        Integer hardwareId = body.get("hardwareId") != null
            ? ((Number) body.get("hardwareId")).intValue()
            : null;
        String title = (String) body.getOrDefault("evaluationTitle", "");
        String perfData = (String) body.getOrDefault("perfTestData", "");
        String usageExp = (String) body.getOrDefault("usageExperience", "");
        String prosCons = (String) body.getOrDefault("prosAndCons", "");
        List<String> imageUrls = body.get("imageUrls") instanceof List
            ? (List<String>) body.get("imageUrls")
            : Collections.emptyList();

        if (hardwareId == null) {
            throw new BusinessException("硬件ID不能为空");
        }
        if (title.isBlank()) {
            throw new BusinessException("评测标题不能为空");
        }

        HardwareInf hw = hardwareInfMapper.selectById(hardwareId);
        if (hw == null) {
            throw new BusinessException(ErrorCode.HARDWARE_NOT_FOUND);
        }

        Evaluation eval = new Evaluation();
        eval.setUserId(userId);
        eval.setHardwareId(hardwareId);
        eval.setEvaluationTitle(title.trim());
        eval.setPerfTestData(perfData);
        eval.setUsageExperience(usageExp);
        eval.setProsAndCons(prosCons);
        eval.setAuditState("pending");
        eval.setPublishTime(LocalDateTime.now());
        evaluationMapper.insert(eval);

        // 保存图片到 Media_Inf
        for (String url : imageUrls) {
            MediaInf media = new MediaInf();
            media.setEvaluationId(eval.getEvaluationId());
            media.setHardwareId(hardwareId);
            media.setMediaUrl(url);
            media.setUploadTime(LocalDateTime.now());
            mediaInfMapper.insert(media);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("evaluationId", eval.getEvaluationId());
        result.put("message", "评测发布成功");
        return Result.success(result);
    }

    /**
     * 获取单篇评测详情（公开，无需认证）
     */
    @GetMapping("/evaluation/{id}")
    public Result<Map<String, Object>> getEvaluationById(@PathVariable Integer id) {
        Evaluation eval = evaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException("评测不存在");
        }

        HardwareInf hw = hardwareInfMapper.selectById(eval.getHardwareId());

        List<MediaInf> evalImages = mediaInfMapper.selectList(
            new QueryWrapper<MediaInf>().eq("evaluation_id", id)
        );

        // 获取发布者信息
        User user = null;
        if (eval.getUserId() != null) {
            user = userMapper.selectOne(
                new QueryWrapper<User>().eq("user_id", eval.getUserId())
            );
        }

        // 获取评论列表
        List<Comment> comments = commentMapper.selectList(
            new QueryWrapper<Comment>()
                .eq("evaluation_id", id)
                .eq("audit_state", "approved")
                .orderByDesc("publish_time")
        );

        Set<String> commentUserIds = comments.stream()
            .map(Comment::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<String, String> commentUserNameMap = Collections.emptyMap();
        if (!commentUserIds.isEmpty()) {
            commentUserNameMap = userMapper.selectList(
                new QueryWrapper<User>().in("user_id", commentUserIds)
            ).stream().collect(Collectors.toMap(User::getUserId, User::getUsername));
        }

        Set<String> commentIds = comments.stream()
            .map(Comment::getCommentId)
            .collect(Collectors.toSet());
        Map<String, List<MediaInf>> commentMediaMap = Collections.emptyMap();
        if (!commentIds.isEmpty()) {
            commentMediaMap = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("comment_id", commentIds)
            ).stream().filter(m -> m.getCommentId() != null)
              .collect(Collectors.groupingBy(MediaInf::getCommentId));
        }

        List<Map<String, Object>> commentList = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String, Object> cmt = new LinkedHashMap<>();
            cmt.put("commentId", comment.getCommentId());
            cmt.put("userId", comment.getUserId());
            cmt.put("content", comment.getContent());
            cmt.put("publishTime", comment.getPublishTime());
            cmt.put("userName", commentUserNameMap.getOrDefault(comment.getUserId(), "匿名用户"));
            List<MediaInf> cmtImages = commentMediaMap.getOrDefault(comment.getCommentId(), Collections.emptyList());
            cmt.put("images", cmtImages.stream().map(MediaInf::getMediaUrl).collect(Collectors.toList()));
            commentList.add(cmt);
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("evaluationId", eval.getEvaluationId());
        item.put("evaluationTitle", eval.getEvaluationTitle());
        item.put("auditState", eval.getAuditState());
        item.put("publishTime", eval.getPublishTime());
        item.put("perfTestData", eval.getPerfTestData());
        item.put("usageExperience", eval.getUsageExperience());
        item.put("prosAndCons", eval.getProsAndCons());
        item.put("images", evalImages.stream().map(MediaInf::getMediaUrl).collect(Collectors.toList()));
        item.put("publisherName", user != null ? user.getUsername() : "匿名用户");
        item.put("comments", commentList);

        if (hw != null) {
            Map<String, Object> hwInfo = new LinkedHashMap<>();
            hwInfo.put("hardwareId", hw.getHardwareId());
            hwInfo.put("hardwareName", hw.getHardwareName());
            hwInfo.put("hardwareType", hw.getHardwareType());
            hwInfo.put("hardwareBrand", hw.getHardwareBrand());
            hwInfo.put("hardwarePrice", hw.getHardwarePrice());

            List<MediaInf> hwImages = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().eq("hardware_id", hw.getHardwareId())
            );
            hwInfo.put("imageUrl", !hwImages.isEmpty() ? hwImages.get(0).getMediaUrl() : null);

            item.put("hardware", hwInfo);
        } else {
            item.put("hardware", null);
        }

        return Result.success(item);
    }

    // ==================== 评论相关 ====================

    /**
     * 获取评测的评论列表（公开）
     */
    @GetMapping("/evaluation/{id}/comments")
    public Result<List<Map<String, Object>>> getComments(@PathVariable Integer id) {
        List<Comment> comments = commentMapper.selectList(
            new QueryWrapper<Comment>()
                .eq("evaluation_id", id)
                .eq("audit_state", "approved")
                .orderByDesc("publish_time")
        );

        if (comments.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Set<String> userIds = comments.stream()
            .map(Comment::getUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<String, String> userNameMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userNameMap = userMapper.selectList(
                new QueryWrapper<User>().in("user_id", userIds)
            ).stream().collect(Collectors.toMap(User::getUserId, User::getUsername));
        }

        Set<String> commentIds = comments.stream()
            .map(Comment::getCommentId)
            .collect(Collectors.toSet());
        Map<String, List<MediaInf>> mediaMap = Collections.emptyMap();
        if (!commentIds.isEmpty()) {
            mediaMap = mediaInfMapper.selectList(
                new QueryWrapper<MediaInf>().in("comment_id", commentIds)
            ).stream().filter(m -> m.getCommentId() != null)
              .collect(Collectors.groupingBy(MediaInf::getCommentId));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String, Object> cmt = new LinkedHashMap<>();
            cmt.put("commentId", comment.getCommentId());
            cmt.put("userId", comment.getUserId());
            cmt.put("content", comment.getContent());
            cmt.put("publishTime", comment.getPublishTime());
            cmt.put("userName", userNameMap.getOrDefault(comment.getUserId(), "匿名用户"));
            List<MediaInf> imgs = mediaMap.getOrDefault(comment.getCommentId(), Collections.emptyList());
            cmt.put("images", imgs.stream().map(MediaInf::getMediaUrl).collect(Collectors.toList()));
            result.add(cmt);
        }

        return Result.success(result);
    }

    /**
     * 发送评论
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/evaluation/{id}/comment")
    public Result<Map<String, Object>> createComment(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        String userId = getCurrentUserId();

        Evaluation eval = evaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException("评测不存在");
        }

        String content = (String) body.getOrDefault("content", "");
        if (content.isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }

        List<String> imageUrls = body.get("imageUrls") instanceof List
            ? (List<String>) body.get("imageUrls")
            : Collections.emptyList();

        String commentId = "CMT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);

        Comment comment = new Comment();
        comment.setCommentId(commentId);
        comment.setEvaluationId(id);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setAuditState("pending");
        comment.setPublishTime(LocalDateTime.now());
        commentMapper.insert(comment);

        for (String url : imageUrls) {
            MediaInf media = new MediaInf();
            media.setCommentId(commentId);
            media.setMediaUrl(url);
            media.setUploadTime(LocalDateTime.now());
            mediaInfMapper.insert(media);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("commentId", commentId);
        result.put("message", "评论发送成功");
        return Result.success(result);
    }

    /**
     * 删除自己的评测
     */
    @DeleteMapping("/evaluation/{id}")
    public Result<Map<String, Object>> deleteEvaluation(@PathVariable Integer id) {
        String userId = getCurrentUserId();

        Evaluation eval = evaluationMapper.selectById(id);
        if (eval == null) {
            throw new BusinessException("评测不存在");
        }
        if (!userId.equals(eval.getUserId())) {
            throw new BusinessException("无权删除此评测");
        }

        // 删除评测关联的评论及其图片
        List<Comment> comments = commentMapper.selectList(
            new QueryWrapper<Comment>().eq("evaluation_id", id)
        );
        for (Comment c : comments) {
            mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("comment_id", c.getCommentId()));
        }
        commentMapper.delete(new QueryWrapper<Comment>().eq("evaluation_id", id));

        // 删除评测关联的图片
        mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("evaluation_id", id));

        evaluationMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "评测已删除");
        return Result.success(result);
    }

    /**
     * 删除自己的评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Map<String, Object>> deleteComment(@PathVariable String id) {
        String userId = getCurrentUserId();

        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException("无权删除此评论");
        }

        // 删除评论关联的图片
        mediaInfMapper.delete(new QueryWrapper<MediaInf>().eq("comment_id", id));
        commentMapper.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "评论已删除");
        return Result.success(result);
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        throw new BusinessException("无法获取当前用户信息");
    }
}
