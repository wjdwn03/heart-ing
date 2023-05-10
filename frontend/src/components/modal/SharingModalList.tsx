import React, { useEffect } from 'react'
import SharingModalListItem from './SharingModalListItem'
import { facebook, kakao, twitter, urlCopy } from '../../assets/images/sharing/sharingIcon'
import { useRecoilValue } from 'recoil';
import { userNicknameAtom } from '../../atoms/userAtoms';

function SharingModalList({...props}) {
  const userNickname = useRecoilValue(userNicknameAtom);
  const url = encodeURI(window.location.href);

  const shareUrl = (e: React.MouseEvent<HTMLDivElement>) => {
    if (navigator.clipboard) {
      // (IE는 사용 못하고, 크롬은 66버전 이상일때 사용 가능합니다.)
      navigator.clipboard
        .writeText(window.location.href)
        .then(() => {
          alert(
            `${userNickname}님의 하트 수신함이 복사되었습니다.\n친구들에게 공유해보세요!`
          );
        })
        .catch(() => {
          alert(`지원하지 않는 브라우저입니다.\n다른 브라우저로 접속해주세요.`);
        });
    } else {
      if (!document.queryCommandSupported("copy")) {
        return alert("복사하기가 지원되지 않는 브라우저입니다.");
      }

      const textarea = document.createElement("textarea");
      textarea.value = window.location.href;

      document.body.appendChild(textarea);
      textarea.focus();
      textarea.select();
      document.execCommand("copy");
      document.body.removeChild(textarea);
      alert(
        `${userNickname}님의 하트 수신함이 복사되었습니다.\n친구들에게 공유해보세요!`
      );
    }
    props.setSharingAtom(false)
  };

  const shareFacebook = (e: React.MouseEvent<HTMLDivElement>) => {
    window.open("http://www.facebook.com/sharer/sharer.php?u=" + url);
    props.setSharingAtom(false)
  }

  const shareTwitter = (e: React.MouseEvent<HTMLDivElement>) => {
    const text = '하트를 보내주세요♡'
    window.open("https://twitter.com/intent/tweet?text=" + text + "&url=" +  url)
    props.setSharingAtom(false)
  }

  const shareKakao = (e: React.MouseEvent<HTMLDivElement>) => {
    // window.Kakao.Share.sendCustom({
    //   templateId: 93587,
    //   templateArgs: {
    //     title: '본격 두근두근 마음 전달 서비스 Hearting!',
    //     description: '하트를 보내주세요♡',
    //   },
    // });
    window.Kakao.Share.sendDefault({
      objectType: 'feed',
      content: {
        title: '본격 두근두근 마음 전달 서비스, 하팅!',
        description: '소중한 사람에게 마음을 전해주세요❤',
        imageUrl: 'https://heart-ing.s3.ap-northeast-2.amazonaws.com/profile/messageLogo.png',
        link: {
          // [내 애플리케이션] > [플랫폼] 에서 등록한 사이트 도메인과 일치해야 함
          mobileWebUrl: 'https://heart-ing.com',
          webUrl: 'https://heart-ing.com',
        },
      },
      itemContent: {
        profileText: 'Hearting!♡',
        profileImageUrl: 'https://heart-ing.s3.ap-northeast-2.amazonaws.com/profile/messageLogo.png',
        titleImageUrl: 'https://heart-ing.s3.ap-northeast-2.amazonaws.com/profile/messageLogo.png',
      },
      buttons: [
        {
          title: '하트 보내러가기',
          link: {
            mobileWebUrl: url,
            webUrl: url,
          },
        },
      ],
    });
    props.setSharingAtom(false)
  }

  const sharetype = [
    {icon : urlCopy, name: 'URL복사', click: shareUrl},
    {icon : facebook, name: '페이스북', click: shareFacebook},
    {icon : twitter, name: '트위터', click: shareTwitter},
    {icon : kakao, name: '카카오톡', click: shareKakao},
  ]
  
  return (
    <div className='flex'>
      {sharetype.map((share: {icon: any, name: string, click: (e: React.MouseEvent<HTMLDivElement>) =>void}) => (
        <SharingModalListItem icon={share.icon} name={share.name} click={share.click}/>
      ))}
    </div>
  )
}

export default SharingModalList