import React, { useEffect, useState } from 'react';
import CountUp from 'react-countup';
import { Button } from '@mui/material';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { Viewer } from '@toast-ui/react-editor';
import styles from './DonationContainer.module.scss';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import ListTable from '../../components/Table/ListTable';
import { requestCurrentDonation } from '../../api/donation';
import { requestUserProfile } from '../../api/user';
import { openModal } from '../../store/slices/donateModalSlice';
import { requestTeamProfileInfo } from '../../api/team';
import { customTextOnlyAlert, noTimeWarn } from '../../utils/customAlert';
import logo from '../../assets/images/logo3.svg';

type ResponseInterface = {
  id: number;
  title: string;
  content: string;
  file: string;
  targetAmount: string;
  currentAmount: string;
  startDate: string;
};

function DonationContainer() {
  const dispatch = useAppDispatch();
  const isLogin = useAppSelector((state) => state.userSlice.isLogin);
  const userId = useAppSelector((state) => state.userSlice.userId);
  const userType = useAppSelector((state) => state.userSlice.userType);
  const [userMoney, setUserMoney] = useState<number>(0);
  const [donBoard, setDonBoard] = useState<ResponseInterface>({
    id: 0,
    title: '',
    content: '',
    file: '',
    targetAmount: '',
    currentAmount: '',
    startDate: '',
  });
  const target = parseInt(donBoard.targetAmount, 10).toLocaleString('ko-KR');
  const diff = (parseInt(donBoard.targetAmount, 10) - parseInt(donBoard.currentAmount, 10)).toLocaleString('ko-KR');

  useEffect(() => {
    fetchData();
    setInitUserMoney();
  }, []);

  const onClickDonation = async () => {
    if (!isLogin) {
      customTextOnlyAlert(noTimeWarn, '로그인이 필요한 서비스입니다.');
      return;
    }
    dispatch(openModal({ isOpen: true, postId: donBoard.id, userId: parseInt(userId, 10), mileage: userMoney }));
  };

  const fetchData = async () => {
    try {
      const response = await requestCurrentDonation();
      console.log('res: ', response);
      console.log('data res: ', response.data);
      setDonBoard(response.data);
    } catch (error) {
      console.log(error);
    }
  };

  // 회원 마일리지 초기 설정
  const setInitUserMoney = async () => {
    try {
      let response;
      if (userType === 'NORMAL' || userType === 'KAKAO') {
        response = await requestUserProfile(userId);
        console.log(response.data.money);
        setUserMoney(response.data.money);
      } else if (userType === 'TEAM') {
        response = await requestTeamProfileInfo(userId);
        console.log(response.data.money);
        setUserMoney(response.data.money);
      }
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.contents}>
        <div className={styles.contentsWrapper}>
          <p className={styles.contentTitle}>
            <img src={logo} alt="logo" />
            에서 진행중인 기부 행사입니다.
          </p>
          <div className={styles['donation-box']}>
            <div className={styles.left}>
              <figure>
                <img src={donBoard.file} alt="donationImage" />
              </figure>
            </div>
            <div className={styles.right}>
              <p className={styles.title}>{donBoard.title}</p>
              {/* <div className={styles.text} dangerouslySetInnerHTML={{ __html: donBoard.content }} /> */}
              <p className={styles.text2}>{donBoard.content && <Viewer initialValue={donBoard.content || ''} />}</p>
              <Button className={styles.donButton} onClick={onClickDonation} type="button">
                기부 참여
              </Button>
            </div>
          </div>
          <div className={styles.inspire}>
            <p>
              목표 금액 <span>{target}</span>원까지 <span>{diff}</span>원 남았습니다.
            </p>
          </div>
          <div className={styles['amount-box']}>
            <p>
              총 후원금 <CountUp start={0} end={parseInt(donBoard.currentAmount, 10)} separator="," duration={4} /> 원
            </p>
          </div>
          <div className={styles.finishedTable}>
            <Accordion sx={{ border: '2px solid rgb(175, 175, 175, 0.5)', marginBottom: '5rem' }}>
              <AccordionSummary expandIcon={<ExpandMoreIcon />} aria-controls="panel1a-content">
                <Typography ml={65} sx={{ fontWeight: '600', fontSize: '20px', opacity: 0.7 }}>
                  이미 종료된 기부 이벤트
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <ListTable />
              </AccordionDetails>
            </Accordion>
          </div>
        </div>
      </div>
    </div>
  );
}
export default DonationContainer;
