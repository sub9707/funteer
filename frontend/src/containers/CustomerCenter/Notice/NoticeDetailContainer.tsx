import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { AiOutlineFileText } from 'react-icons/ai';
import { BsFillImageFill } from 'react-icons/bs';
import '@toast-ui/editor/dist/toastui-editor.css';
import { Viewer } from '@toast-ui/react-editor';
import { requestNoticeDetail } from '../../../api/admin';
import styles from './NoticeDetailContainer.module.scss';
import { useAppSelector } from '../../../store/hooks';

const imgExpansion = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg', '.jfif', '.tif', '.tiff'];

export type NoticeDetailType = {
  id: string;
  title: string;
  content: string;
  localDate: string;
  files: Map<string, string>[];
};

function NoticleDetailContainer() {
  const navigate = useNavigate();
  const location = useLocation();
  const userType = useAppSelector((state) => state.userSlice.userType);
  const data = location.state.data as NoticeDetailType;
  const [NoticeDetail, setNoticeDetail] = useState<NoticeDetailType>({
    id: '',
    title: '',
    content: '',
    localDate: data.localDate,
    files: [],
  });

  useEffect(() => {
    requestNotice();
  }, []);

  const isImage = (fileUrl: string) => {
    // eslint-disable-next-line
    for (let i = 0; i < imgExpansion.length; i++) {
      if (fileUrl.endsWith(imgExpansion[i])) {
        return true;
      }
    }
    return false;
  };

  const onClickEditBtn = () => {
    navigate('edit', {
      state: {
        NoticeDetail,
      },
    });
  };

  const requestNotice = async () => {
    try {
      const response = await requestNoticeDetail(data.id);
      console.log('공지사항 디테일 정보 요청', response);
      setNoticeDetail({ ...response.data, localDate: data.localDate });
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.contents}>
        <h1 className={styles.title}>{NoticeDetail.title}</h1>
        <p className={styles['post-date']}>{NoticeDetail.localDate}</p>
        <hr />
        <div className={styles.content}>{NoticeDetail.content && <Viewer initialValue={NoticeDetail.content || ''} />}</div>
        <hr />
        <p className={styles['post-date']}>첨부파일</p>
        {NoticeDetail.files.map((file) => (
          <a href={Object.values(file)[0]} className={styles.file}>
            {isImage(Object.values(file)[0]) && <BsFillImageFill style={{ marginRight: '0.5rem' }} />}
            {!isImage(Object.values(file)[0]) && <AiOutlineFileText style={{ marginRight: '0.4rem' }} />}
            {Object.keys(file)[0]}
          </a>
        ))}
        <div>
          <button type="button" className={styles.back} onClick={() => navigate(-1)}>
            {'< 목록으로'}
          </button>
          {userType === 'ADMIN' && (
            <button type="button" className={styles.edit} onClick={onClickEditBtn}>
              수정
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default NoticleDetailContainer;
