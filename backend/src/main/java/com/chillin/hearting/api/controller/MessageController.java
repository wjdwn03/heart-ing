package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.Data;
import com.chillin.hearting.api.request.ReportReq;
import com.chillin.hearting.api.request.SendMessageReq;
import com.chillin.hearting.api.response.ResponseDTO;
import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final String SUCCESS = "success";
    private final MessageService messageService;
    private final HeartService heartService;

    @PostMapping("")
    public ResponseEntity<ResponseDTO> sendMessage(@Valid @RequestBody SendMessageReq sendMessageReq, HttpServletRequest httpServletRequest) {


        User user = (User) httpServletRequest.getAttribute("user");

        // Check if logged-in user is same as sender
        if (user != null && !user.getId().equals(sendMessageReq.getSenderId())) {
            throw new WrongUserException();
        }

        // Check if sent to myself
        if (user != null && user.getId().equals(sendMessageReq.getReceiverId())) {
            throw new WrongUserException("본인에게 메시지를 보냈습니다.");
        }

        // Get client IP
        String clientIp = httpServletRequest.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = httpServletRequest.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0];
        }
        log.debug("Client request from : " + clientIp);

        Data data = messageService.sendMessage(sendMessageReq.getHeartId(), sendMessageReq.getSenderId(), sendMessageReq.getReceiverId(), sendMessageReq.getTitle(), sendMessageReq.getContent(), clientIp);

        if (user != null) heartService.updateHeartCondition(sendMessageReq.getSenderId(), sendMessageReq.getHeartId());

        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(SUCCESS)
                .message("메시지가 성공적으로 발송되었습니다.")
                .data(data).build();

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ResponseDTO> deleteMessage(@PathVariable("messageId") long messageId, HttpServletRequest httpServletRequest) {
        User user = (User) httpServletRequest.getAttribute("user");

        // Check if user has permissions
        if (user == null) {
            throw new UnAuthorizedException();
        }

        boolean returnedIsActive = messageService.deleteMessage(messageId, user.getId());
        if (returnedIsActive) {
            throw new DeleteMessageFailException();
        }

        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(SUCCESS)
                .message("메시지가 성공적으로 삭제되었습니다.")
                .build();

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping("/{messageId}/reports")
    public ResponseEntity<ResponseDTO> reportMessage(@Valid @RequestBody ReportReq reportReq, @PathVariable("messageId") long messageId, HttpServletRequest httpServletRequest) {

        User user = (User) httpServletRequest.getAttribute("user");

        // Check if user has permissions
        if (user == null) {
            throw new UnAuthorizedException();
        }

        Long returnedReportId = messageService.reportMessage(messageId, user.getId(), reportReq.getContent());
        if (returnedReportId == null) {
            throw new ReportFailException();
        }

        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(SUCCESS)
                .message("메시지가 성공적으로 신고되었습니다.")
                .build();

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/{messageId}/emojis/{emojiId}")
    public ResponseEntity<ResponseDTO> addEmoji(@PathVariable("messageId") long messageId, @PathVariable("emojiId") long emojiId, HttpServletRequest httpServletRequest) {

        User user = (User) httpServletRequest.getAttribute("user");

        // Check if user has permissions
        if (user == null) {
            throw new UnAuthorizedException();
        }

        Data returnedEmojiData = messageService.addEmoji(messageId, user.getId(), emojiId);
        if (returnedEmojiData == null) {
            throw new EmojiFailException();
        }

        ResponseDTO responseDTO = ResponseDTO.builder()
                .status(SUCCESS)
                .message("이모지가 성공적으로 변경되었습니다.")
                .data(returnedEmojiData)
                .build();

        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


}
