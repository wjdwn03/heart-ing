package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.InboxData;
import com.chillin.hearting.api.data.InboxDetailData;
import com.chillin.hearting.api.response.ResponseDTO;
import com.chillin.hearting.api.service.InboxService;
import com.chillin.hearting.db.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;

    private final String INBOX_STORE_SUCCESS = "메시지 영구 보관 저장을 성공했습니다.";
    private final String INBOX_FIND_SUCCESS = "영구 보관 메시지 리스트 조회를 성공했습니다.";
    private final String INBOX_DETAIL_FIND_SUCCESS = "영구 보관 메시지 상세 조회를 성공했습니다.";
    private final String INBOX_DELETE_SUCCESS = "영구 보관 메시지 삭제를 성공했습니다.";

    @PostMapping("/{messageId}")
    public ResponseEntity<ResponseDTO> storeMessageToInbox(@PathVariable("messageId") Long messageId, HttpServletRequest httpServletRequest) {
        inboxService.storeMessage(messageId);

        ResponseDTO responseDTO = ResponseDTO.builder().status("success").message(INBOX_STORE_SUCCESS).build();
        return new ResponseEntity<ResponseDTO>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDTO> findInboxMessages(HttpServletRequest httpServletRequest) {
        User user = (User) httpServletRequest.getAttribute("user");
        InboxData inboxData = inboxService.findInboxMessages(user.getId());

        ResponseDTO responseDTO = ResponseDTO.builder().status("success").data(inboxData).message(INBOX_FIND_SUCCESS).build();
        return new ResponseEntity<ResponseDTO>(responseDTO, HttpStatus.OK);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ResponseDTO> findInboxDetailMessage(@PathVariable("messageId") Long messageId, HttpServletRequest httpServletRequest) {
        User user = (User) httpServletRequest.getAttribute("user");
        InboxDetailData inboxDetailData = inboxService.findInboxDetailMessage(user.getId(), messageId);

        ResponseDTO responseDTO = ResponseDTO.builder().status("success").data(inboxDetailData).message(INBOX_DETAIL_FIND_SUCCESS).build();
        return new ResponseEntity<ResponseDTO>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ResponseDTO> deleteInboxMessage(@PathVariable("messageId") Long messageId, HttpServletRequest httpServletRequest) {
        inboxService.deleteMessage(messageId);

        ResponseDTO responseDTO = ResponseDTO.builder().status("success").message(INBOX_DELETE_SUCCESS).build();
        return new ResponseEntity<ResponseDTO>(responseDTO, HttpStatus.CREATED);
    }
}
