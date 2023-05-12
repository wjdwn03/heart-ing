import { axios } from "./https";
import {
  IMessageSendTypes,
  IResponseHeartTypes,
} from "../../types/messageType";

export async function getReceived(userId: string) {
  try {
    const res = await axios.get(`api/v1/messages/received/${userId}`);
    const data = res.data;
    return data;
  } catch (err) {
    // console.log("24시간 내 받은 메시지 조회에 실패했습니다.");
    // console.log(err);
    return null;
  }
}

export async function sendMessageApi(body: IMessageSendTypes) {
  try {
    const res = await axios.post(`api/v1/messages`, body);
    const data = res.data;
    return data;
  } catch (err) {
    // console.log(err);
    return err;
  }
}

export async function getMessageDetail(messageId: number) {
  try {
    const res = await axios.get(`api/v1/messages/received/detail/${messageId}`);
    const data = res.data;
    return data;
  } catch (err) {
    // console.log("상세 메시지 보기에 실패했습니다");
    // console.log(err);
    return null;
  }
}

export async function saveMessageApi(messageId: number) {
  try {
    const res = await axios.post(`api/v1/messages/inbox/${messageId}`);
    const status = res.data.status;
    return status;
  } catch (err) {
    // console.log("저장 못함ㅠ");
    return null;
  }
}

export async function deletepermanentMessageApi(messageId: number) {
  try {
    const res = await axios.delete(`api/v1/messages/inbox/${messageId}`);
    const status = res.data.status;
    return status;
  } catch (err) {
    // console.log("영구 삭제 못함ㅠ");
    return null;
  }
}

export async function deleteTemporaryMessageApi(messageId: number) {
  try {
    const res = await axios.delete(`api/v1/messages/${messageId}`);
    const status = res.data.status;
    return status;
  } catch (err) {
    // console.log("24시간 삭제 못함ㅠ");
    return null;
  }
}

export async function getSave() {
  try {
    const res = await axios.get(`api/v1/messages/inbox`);
    const data = res.data;
    return data;
  } catch (err) {
    // console.log("영구 보관 메시지 리스트 조회에 실패했습니다");
    // console.log(err);
    return null;
  }
}

export async function getSent() {
  try {
    const res = await axios.get(`api/v1/messages/sent`);
    const data = res.data;
    return data;
  } catch (err) {
    // console.log("보낸 메시지 리스트 조회에 실패했습니다");
    // console.log(err);
    return null;
  }
}

export async function getSentMessageDetailApi(messageId: number) {
  try {
    const res = await axios.get(`api/v1/messages/sent/${messageId}`);
    const data = res.data;
    // console.log("내가 보낸거 확인~", data)
    return data;
  } catch (err) {
    // console.log("내가 보낸 거 못 받았당", err);
    return null;
  }
}

export async function getMessageHeartApi() {
  try {
    const res = await axios.get("api/v1/hearts/user-hearts");
    const data = res.data;
    return data;
  } catch (err) {
    // console.log("하트리스트 못 뽑았다여");
    // console.log(err);
    return null;
  }
}

export async function getTotalHeartApi() {
  try {
    const res = await axios.get("api/v1/home/total-count");
    const data = res.data;
    // console.log("이 만큼~", data.data.totalHeartCount)
    return data.data.totalHeartCount;
  } catch (err) {
    // console.log("오 토탈 하트 갯수 안왔는뎅~");
    // console.log(err);
    return null;
  }
}

export async function responseHeartApi({
  messageId,
  emojiId,
}: IResponseHeartTypes) {
  try {
    const res = await axios.post(
      `api/v1/messages/${messageId}/emojis/${emojiId}`
    );
    const data = res.data;
    return data;
  } catch (err) {
    return null;
  }
}

export async function reportMessageApi(messageId: number, body: string) {
  try {
    const res = await axios.post(`api/v1/messages/${messageId}/reports`, body);
    const data = res.data;
    return data;
  } catch (err) {
    return err;
  }
}