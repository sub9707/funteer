import { createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { RootState } from '../store';

export interface UserStateInterface {
  isLogin: boolean;
  userType: string;
  userId: string;
  username: string;
  profileImgUrl: string;
}

const initialState: UserStateInterface = {
  isLogin: false,
  userType: '',
  userId: '',
  username: '',
  profileImgUrl: '',
};

export const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setUserLoginState: (state, action: PayloadAction<UserStateInterface>) => {
      state.isLogin = action.payload.isLogin;
      state.userType = action.payload.userType;
      state.userId = action.payload.userId;
      state.username = action.payload.username;
      state.profileImgUrl = action.payload.profileImgUrl;
    },
    setUserType: (state, action: PayloadAction<string>) => {
      state.userType = action.payload;
    },
    resetLoginState: (state) => {
      state.isLogin = false;
      state.userType = '';
      state.userId = '';
      state.username = '';
    },
  },
});

export const { setUserLoginState, setUserType, resetLoginState } = userSlice.actions;

export const isLoginState = (state: RootState) => state.userSlice.isLogin;
export const profileImgState = (state: RootState) => state.userSlice.profileImgUrl;

export default userSlice.reducer;
