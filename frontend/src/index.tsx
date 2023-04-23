import React, { useEffect, Suspense, lazy } from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import { BrowserRouter, createBrowserRouter, RouterProvider, useParams, useSearchParams } from 'react-router-dom';
import { Provider } from 'react-redux';
import { ThemeProvider } from '@emotion/react';
import { persistStore } from 'redux-persist';
import { PersistGate } from 'redux-persist/integration/react';
import { config } from 'yargs';
import App from './App';
import reportWebVitals from './reportWebVitals';
import store from './store/store';
/*  */
import { theme } from './theme/theme';
import UserRoot from './roots/UserRoot';
import AdminRoot from './roots/AdminRoot';
import UserFooterRoot from './roots/UserFooterRoot';
import {
  MainPage,
  SignUp,
  MemberSignUp,
  TeamSignUp,
  FindPassword,
  Login,
  DevTeamPage,
  ServiceDetail,
  FindEmail,
  ResetPassword,
  Charge,
  Donation,
  ErrorPage,
  MyBadges,
  EditProfile,
  MyDonates,
  MyFavors,
  MyFollows,
  MyFunding,
  MyFunteerDonate,
  MyPage,
  AdminMain,
  AdminMember,
  AdminNoticeCreate,
  LogOut,
  AdminTeam,
  FundingList,
  CreateFunding,
  AdminTeamDeny,
  AdminFunding,
  NoticeDetail,
  AdminDonation,
  AdminDonationCreate,
  AdminDonationDetail,
  TeamProfile,
  AdminNotice,
  TeamEdit,
  TeamDonation,
  Kakao,
  CreateLive,
  PublisherLiveRoom,
  SubscribeLiveRoom,
  ModifyFunding,
  AdminFundingReject,
  FAQDetail,
  ChargeCancel,
  NoticeList,
  FAQList,
  QuestionList,
  NoticeEdit,
  LiveList,
  FAQCreate,
  FAQEdit,
  QuestionCreate,
  QuestionDetail,
  NotFound,
} from './pages/index';
import FundingDetail from './pages/Funding/FundingDetail';
import LiveTest from './containers/MyPage/LiveTest';
import { http } from './api/axios';
import ScrollToTop from './utils/ScrollToTop';


const router = createBrowserRouter([
  /** Footer 없는 페이지 */
  {
    path: '/',
    element: <UserRoot />,
    errorElement: <NotFound />,
    children: [
      {
        path: 'login',
        element: <Login />,
      },
      {
        path: 'login/kakao',
        element: <Kakao />,
      },
      {
        path: 'findEmail',
        element: <FindEmail />,
      },
      {
        path: 'findPassword',
        element: <FindPassword />,
      },
      {
        path: 'resetPassword',
        element: <ResetPassword />,
      },
      {
        path: 'signup',
        element: <SignUp />,
      },
      {
        path: 'signup/team',
        element: <TeamSignUp />,
      },
      {
        path: 'signup/member',
        element: <MemberSignUp />,
      },
      {
        path: 'logout',
        element: <LogOut />,
      },
      {
        path: 'service',
        element: <ServiceDetail />,
      },
      {
        path: 'devteam',
        element: <DevTeamPage />,
      },
      {
        path: '/test',
        element: <LiveTest />,
      },
      {
        path: 'myPage',
        element: <MyPage />,
      },
      {
        path: 'editProfile',
        element: <EditProfile />,
      },
      {
        path: 'myFunding',
        element: <MyFunding />,
      },
      {
        path: 'myFunteerDonate',
        element: <MyFunteerDonate />,
      },
      {
        path: 'myDonates',
        element: <MyDonates />,
      },
      {
        path: 'myBadges',
        element: <MyBadges />,
      },
      {
        path: 'myFavors',
        element: <MyFavors />,
      },
      {
        path: 'myFollow',
        element: <MyFollows />,
      },
      {
        path: 'live',
        element: <LiveList />,
      },
    ],
  },
  {
    path: '/',
    children: [
      {
        path: 'createLive',
        element: <CreateLive />,
      },
      {
        path: 'publisherLiveRoom/:username',
        element: <PublisherLiveRoom />,
      },
      {
        path: 'subscribeLiveRoom/:sessionName',
        element: <SubscribeLiveRoom />,
      },
    ],
  },
  /** Footer 있는 페이지 */
  {
    path: '/',
    element: <UserFooterRoot />,
    errorElement: <NotFound />,
    children: [
      {
        index: true,
        element: <MainPage />,
      },
      {
        path: 'donation',
        element: <Donation />,
      },
      {
        path: 'charge',
        element: <Charge />,
      },
      {
        path: 'charge/cancel',
        element: <ChargeCancel />,
      },
      {
        path: '/funding',
        element: <FundingList />,
      },
      {
        path: '/funding/create',
        element: <CreateFunding />,
      },
      {
        path: '/funding/detail/:fundIdx',
        element: <FundingDetail />,
      },
      {
        path: '/funding/modify/:fundIdx',
        element: <ModifyFunding />,
      },
      {
        path: '/notice',
        element: <NoticeList />,
      },
      {
        path: '/notice/:noticeId',
        element: <NoticeDetail />,
      },
      {
        path: '/notice/:noticeId/edit',
        element: <NoticeEdit />,
      },
      {
        path: '/faq',
        element: <FAQList />,
      },
      {
        path: '/qna',
        element: <QuestionList />,
      },
      {
        path: '/faq/:faqId',
        element: <FAQDetail />,
      },
      {
        path: '/faq/create',
        element: <FAQCreate />,
      },
      {
        path: '/faq/:faqId/edit',
        element: <FAQEdit />,
      },
      {
        path: '/qna/create',
        element: <QuestionCreate />,
      },
      {
        path: '/qna/:qnaId',
        element: <QuestionDetail />,
      },
      {
        path: 'team/:teamId',
        element: <TeamProfile />,
      },
      {
        path: 'teamedit/:teamId',
        element: <TeamEdit />,
      },
      {
        path: 'teamdonation/:teamId',
        element: <TeamDonation />,
      },
    ],
  },
  /** 관리자 페이지 */
  {
    path: '/admin',
    element: <AdminRoot />,
    errorElement: <NotFound />,
    children: [
      {
        index: true,
        element: <AdminMain />,
      },
      {
        path: 'member',
        element: <AdminMember />,
      },
      {
        path: 'team',
        element: <AdminTeam />,
      },
      {
        path: 'team/deny/:dn', // dn: 가입 거부된 팀 번호
        element: <AdminTeamDeny />,
      },
      {
        path: 'funding',
        element: <AdminFunding />,
      },
      {
        path: 'donation',
        element: <AdminDonation />,
      },
      {
        path: 'donation/create',
        element: <AdminDonationCreate />,
      },
      {
        path: 'donation/:dn',
        element: <AdminDonationDetail />,
      },
      {
        path: 'notice',
        element: <AdminNotice />,
      },
      {
        path: 'notice/noticecreate',
        element: <AdminNoticeCreate />,
      },
      {
        path: 'funding/reject/:id',
        element: <AdminFundingReject />,
      },
    ],
  },
]);

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
const persistor = persistStore(store);

root.render(
  <Provider store={store}>
    <PersistGate loading={null} persistor={persistor}>
        <RouterProvider router={router} />
    </PersistGate>
  </Provider>,
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
