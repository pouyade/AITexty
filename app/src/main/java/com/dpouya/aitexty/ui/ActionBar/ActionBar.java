package com.dpouya.aitexty.ui.ActionBar;

/**
 * Created by pouyadark on 2/20/19.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.dpouya.aitexty.R;
import com.dpouya.aitexty.components.FaIconView;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.FontAwesome;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.Cell.ActionBarButtonListCell;

import java.util.List;


/**
 * Created by pouyadark on 11/26/18.
 */

public class ActionBar extends FrameLayout {


    private static ActionBar instance;
    private Typeface typeFace;
    private int ForeGroundColor=0xffffffff;
    private int alphaForeGroundColor=0xaaffffff;
    private int SearchHintColor=0xbbffffff;
    private int SearchBackGround=0xbb6F1E51;

    private boolean searchmode=false;
    private String title=getContext().getResources().getString(R.string.app_name);
    private String subtitle;
    private TextView txtTitle;
    private TextView txtSubTitle;
    private EditText txtSearch;
    private FaIconView imgIcon;
    private FaIconView imgotherIcon;
    private Activity attachedActivity;
    private String searchhint = "Search ...";
    private FaIconView imgGoSearchIcon;
    private View Extraview;

    public boolean showBackButton=false;
    public boolean showDrawerMenuicon=false;
    private OnClickListener onIconClick;

    private int backGroundColor=0;
    public int mActionBarSize = 0;
    private int riplebackgroundeffectResourse;
    private int primaryColor;
    private TabLayout tabLayout;
    private ActionBarButtonListCell actionBarButtonListCell;
    private boolean eshowicon;
    private boolean eshowothericon;
    private FontAwesome.Icon icondrawble;
    private FontAwesome.Icon iconotherdrawble;
    private FrameLayout customviewframe;
    public boolean drawline=false;
    private View line;
    private TextView txtCenterTitle;
    private OnClickListener onOtherIconClick;
    private int titleColor;
    private int subTitlecolor;
    private boolean isRTL = false;
    private View customview;
    private com.dpouya.aitexty.ui.widgets.ContactAvatarView contactAvatarView;
    private boolean showContactAvatar = false;
    private String contactAddress;
    private String contactDisplayName;

    public ActionBar( Context context) {
        super(context);
        this.attachedActivity= (Activity) context;
        this.typeFace = Typeface.DEFAULT;
        init();
    }
    public ActionBar( Context context, AttributeSet attrs) {
        super(context, attrs);
        this.typeFace = Typeface.DEFAULT;
        init();
    }
    public ActionBar( Context context, View Extraview) {
        super(context);
        this.Extraview=Extraview;
        this.typeFace = Typeface.DEFAULT;
        this.attachedActivity= (Activity) context;
        init();
    }

    public static Context getContextInstance() {
        return instance.getContext();
    }

    public void setForeGroundColor(int foreGroundColor) {
        ForeGroundColor = foreGroundColor;
        alphaForeGroundColor = AndroidUtilities.adjustAlpha(foreGroundColor,0.6f);

        tabLayout.setTabTextColors(alphaForeGroundColor,ForeGroundColor);
        tabLayout.setSelectedTabIndicatorColor(ForeGroundColor);
        applyFaIcon(imgGoSearchIcon, FontAwesome.Icon.SEARCH, ForeGroundColor);
        txtSearch.setTextColor(ForeGroundColor);
        if(eshowicon) {
            applyFaIcon(imgIcon, icondrawble, titleColor != 0 ? titleColor : ForeGroundColor);
        }
        if(eshowothericon){
            applyFaIcon(imgotherIcon, iconotherdrawble, ForeGroundColor);
        }
    }

    private void applyFaIcon(FaIconView view, FontAwesome.Icon icon, int color) {
        if (view == null || icon == null) {
            return;
        }
        view.setIcon(icon);
        view.setIconColor(color);
    }

    public ActionBar( Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setSearchhint(String searchhint) {
        this.searchhint = searchhint;
        this.txtSearch.setHint(searchhint);
    }

    public void setTitle(String title) {
        this.title = title;
        txtTitle.setText(title);
        recreateLayout();
    }
    public void setSubTitle(String subtitle) {
        this.subtitle = subtitle;
        txtSubTitle.setText(subtitle);
        recreateLayout();
    }
    public void clearTabs(){
        tabLayout.removeAllTabs();
        tabLayout.setVisibility(GONE);

    }
    public void setupTabs(List<String> tabs, TabLayout.BaseOnTabSelectedListener tablistener){
        tabLayout.setVisibility(VISIBLE);
        tabLayout.removeAllTabs();
        for (int i = 0; i < tabs.size(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(tabs.get(i));
            tabLayout.addTab(tab);
        }
        changeTabsFont(tabLayout);
        recreateLayout();
        tabLayout.setOnTabSelectedListener(tablistener);
    }
    public void setupTabs(List<String> tabs){
        tabLayout.setVisibility(VISIBLE);
        tabLayout.removeAllTabs();
        for (int i = 0; i < tabs.size(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(tabs.get(i));
            tabLayout.addTab(tab);
        }
        changeTabsFont(tabLayout);
        recreateLayout();
    }

    public void setupTabs(ViewPager pager){
        tabLayout.setVisibility(VISIBLE);
        tabLayout.removeAllTabs();
        tabLayout.setupWithViewPager(pager);
        changeTabsFont(tabLayout);
        recreateLayout();
    }
    public void setCenteredText(String text){
        txtCenterTitle.setVisibility(VISIBLE);
        txtCenterTitle.setText(text);
        txtTitle.setVisibility(GONE);
        txtSubTitle.setVisibility(GONE);
    }
    public void setCenteredText(String text,Typeface font,int sizeSp){
        txtCenterTitle.setVisibility(VISIBLE);
        txtCenterTitle.setText(text);
        txtCenterTitle.setTypeface(font);
        txtCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,sizeSp);
        txtTitle.setVisibility(GONE);
        txtSubTitle.setVisibility(GONE);
    }
    public void hideCenteredText(){
        txtCenterTitle.setVisibility(GONE);
        txtTitle.setVisibility(VISIBLE);
        txtSubTitle.setVisibility(VISIBLE);
    }
    public void showCustomView(View view){
        this.customview = view;
        customviewframe.removeAllViews();
        customviewframe.addView(view, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.MATCH_PARENT,isRTL?Gravity.LEFT:Gravity.RIGHT));
        recreateLayout();

    }
    private void init() {
        instance = this;
        getDefaults();
        isRTL= LocaleController.isRTL;
        imgIcon=new FaIconView(getContext(), 20);
        imgIcon.setBackgroundResource(riplebackgroundeffectResourse);
        imgIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onIconClick!=null){
                    onIconClick.onClick(view);
                }
            }
        });

        imgotherIcon=new FaIconView(getContext(), 20);
        imgotherIcon.setBackgroundResource(riplebackgroundeffectResourse);
        imgotherIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onOtherIconClick!=null){
                    onOtherIconClick.onClick(view);
                }
            }
        });

        txtTitle=new TextView(getContext());
        txtTitle.setTextColor(ForeGroundColor);
        txtTitle.setTypeface(this.typeFace);
        txtTitle.setGravity(Gravity.CENTER_VERTICAL|(isRTL?Gravity.RIGHT:Gravity.LEFT));
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        txtTitle.setPadding(AndroidUtilities.dp10,0,AndroidUtilities.dp10,0);
        txtTitle.setText(title);
        addView(txtTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.MATCH_PARENT,
                Gravity.CENTER_VERTICAL|(isRTL?Gravity.RIGHT:Gravity.LEFT),0,0,0,0));

        txtCenterTitle=new TextView(getContext());
        txtCenterTitle.setTextColor(ForeGroundColor);
        txtCenterTitle.setVisibility(GONE);
        txtCenterTitle.setPadding(0,AndroidUtilities.dp5,0,0);
        txtCenterTitle.setTypeface(this.typeFace);
        txtCenterTitle.setGravity(Gravity.CENTER_VERTICAL);
        txtCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        addView(txtCenterTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL|Gravity.CENTER,0,0,0,0));


        txtSubTitle=new TextView(getContext());
        txtSubTitle.setTextColor(alphaForeGroundColor);
        txtSubTitle.setTypeface(this.typeFace);
        txtSubTitle.setGravity(Gravity.CENTER_VERTICAL|(isRTL?Gravity.RIGHT:Gravity.LEFT));
        txtSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        txtSubTitle.setVisibility(GONE);
        txtSubTitle.setText(subtitle);
        txtSubTitle.setPadding(AndroidUtilities.dp10,0,AndroidUtilities.dp10,0);
        addView(txtSubTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.MATCH_PARENT,
                Gravity.CENTER_VERTICAL|(isRTL?Gravity.RIGHT:Gravity.LEFT),0,0,0,0));

        txtSearch=new EditText(getContext());

        txtSearch.setTextColor(ForeGroundColor);
        txtSearch.setTypeface(this.typeFace);
        txtSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        txtSearch.setGravity(Gravity.CENTER_VERTICAL|(isRTL?Gravity.RIGHT:Gravity.LEFT));
        txtSearch.setHint(searchhint);
        txtSearch.setBackground(null);
        txtSearch.setHintTextColor(SearchHintColor);
        txtSearch.setVisibility(GONE);
        addView(txtSearch,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,mActionBarSize,
                Gravity.CENTER_VERTICAL|(isRTL?Gravity.RIGHT:Gravity.LEFT),50,0,0,0));


        addView(imgIcon, LayoutHelper.createFrame(mActionBarSize, mActionBarSize, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 0, 0, 0, 0));
        addView(imgotherIcon, LayoutHelper.createFrame(mActionBarSize, mActionBarSize, Gravity.CENTER_VERTICAL | Gravity.RIGHT, mActionBarSize+5, 0, mActionBarSize+5, 0));

        imgIcon.setPadding(AndroidUtilities.dp10,AndroidUtilities.dp10,AndroidUtilities.dp10,AndroidUtilities.dp10);
        imgotherIcon.setPadding(AndroidUtilities.dp10,AndroidUtilities.dp10,AndroidUtilities.dp10,AndroidUtilities.dp10);


        imgGoSearchIcon=new FaIconView(getContext(), 18);
        imgGoSearchIcon.setVisibility(GONE);
        applyFaIcon(imgGoSearchIcon, FontAwesome.Icon.SEARCH, ForeGroundColor);
        addView(imgGoSearchIcon,LayoutHelper.createFrame(30,mActionBarSize, Gravity.CENTER_VERTICAL|Gravity.LEFT,5,0,5,0));

        actionBarButtonListCell=new ActionBarButtonListCell(getContext(),this.typeFace);
        addView(actionBarButtonListCell,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,mActionBarSize, Gravity.TOP|Gravity.LEFT));

        tabLayout=new TabLayout(getContext());
        tabLayout.setVisibility(GONE);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setSelectedTabIndicatorColor(ForeGroundColor);
        tabLayout.setSelectedTabIndicatorHeight(AndroidUtilities.dp2);
        tabLayout.setTabTextColors(alphaForeGroundColor,ForeGroundColor);
        addView(tabLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,mActionBarSize,Gravity.TOP|Gravity.RIGHT));

        customviewframe=new FrameLayout(getContext());
        customviewframe.setVisibility(GONE);
        addView(customviewframe,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,mActionBarSize,Gravity.TOP|Gravity.LEFT));
        if(drawline) {
            line = new View(getContext());
            line.setBackgroundColor(0xbbcccccc);
            addView(line, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM));
        }
        contactAvatarView = com.dpouya.aitexty.ui.widgets.ContactAvatarView.create(getContext(),
                com.dpouya.aitexty.ui.widgets.ContactAvatarView.SIZE_ACTIONBAR);
        contactAvatarView.setVisibility(GONE);
        addView(contactAvatarView, LayoutHelper.createFrame(
                com.dpouya.aitexty.ui.widgets.ContactAvatarView.SIZE_ACTIONBAR,
                com.dpouya.aitexty.ui.widgets.ContactAvatarView.SIZE_ACTIONBAR,
                Gravity.CENTER_VERTICAL | Gravity.LEFT,
                mActionBarSize, 0, 0, 0));
        this.updateColors();

        setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,mActionBarSize));
    }
    public void reset(){
        if(searchmode)setSearchmode(false);
        actionBarButtonListCell.ClearButtons();
        txtCenterTitle.setVisibility(GONE);
        customviewframe.setVisibility(GONE);
        customviewframe.removeAllViews();
        showotherIcon(false,null);
        clearTabs();
    }
    public void setContactAvatar(String address, String displayName) {
        contactAddress = address;
        contactDisplayName = displayName;
        showContactAvatar = address != null && !address.isEmpty();
        if (showContactAvatar && contactAvatarView != null) {
            contactAvatarView.bind(address, displayName);
        }
        recreateLayout();
    }

    public void clearContactAvatar() {
        showContactAvatar = false;
        if (contactAvatarView != null) {
            contactAvatarView.setVisibility(GONE);
        }
        recreateLayout();
    }

    public void setSearchmode(boolean searchmode) {
        this.searchmode = searchmode;
        recreateLayout();
    }

    public void addButton(ActionBarButton actionbarButton){
        AndroidUtilities.ChangeGravitiy(actionBarButtonListCell,isRTL?Gravity.LEFT:Gravity.RIGHT);
        actionBarButtonListCell.addItem(actionbarButton);
    }

    public void setExtraView(View extraView) {
        this.Extraview = extraView;
        if(Extraview!=null){
//            Extraview.setBackgroundColor(BackGround);
            addView(Extraview,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.TOP,
                    0,mActionBarSize,0,0));
        }
    }

    public void showDrawerMenuicon(boolean show) {
        showDrawerMenuicon=show;
        showIcon(show, FontAwesome.Icon.BARS);
        recreateLayout();
    }

    private void showIcon(boolean show, FontAwesome.Icon icon){
        eshowicon=show;
        icondrawble=icon;
        if(eshowicon) {
            applyFaIcon(imgIcon, icondrawble, titleColor != 0 ? titleColor : ForeGroundColor);
        }
        recreateLayout();
    }
    private void recreateLayout(){
        boolean hasSubtitle= subtitle!=null&&subtitle.length()>0;
        AndroidUtilities.ChangeGravitiy(imgIcon,isRTL?Gravity.RIGHT:Gravity.LEFT);
        AndroidUtilities.ChangeGravitiy(txtTitle,(isRTL?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL);
        txtTitle.setVisibility(title.length()>0?VISIBLE:GONE);
        if(eshowothericon) {
            applyFaIcon(imgotherIcon, iconotherdrawble, ForeGroundColor);
        }

        txtTitle.measure(0,0);
        int widthpx = txtTitle.getMeasuredWidth();
        int titlewidth=AndroidUtilities.pxtodp(getContext(),widthpx);

//        AndroidUtilities.ChangeGravitiy(tabLayout,(isRTL?Gravity.LEFT:Gravity.RIGHT));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        int rightMargin = isRTL?(showBackButton?mActionBarSize+5:0):0;
        int leftMargin = !isRTL?(showBackButton?mActionBarSize+5:0):0;
        tabLayout.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,mActionBarSize,Gravity.TOP|(isRTL?Gravity.LEFT:Gravity.RIGHT),leftMargin,0,rightMargin,0));

//        if(isRTL) {
//            AndroidUtilities.ChangeMarginRight(txtSearch, eshowicon ? 50 : 0);
//            AndroidUtilities.ChangeMarginLeft(txtSearch, 40);
//            AndroidUtilities.ChangeMarginRight(txtSearch, 0);
//            AndroidUtilities.ChangeMarginRight(tabLayout, titlewidth + (eshowicon ? 50 : 0));
//        }else{
//            AndroidUtilities.ChangeMarginLeft(txtSearch, eshowicon ? 50 : 0);
//            AndroidUtilities.ChangeMarginLeft(txtSearch, 0);
//            AndroidUtilities.ChangeMarginRight(txtSearch, 40);
//            AndroidUtilities.ChangeMarginLeft(tabLayout, titlewidth + (eshowicon ? 50 : 0));
//        }
        AndroidUtilities.ChangeGravitiy(txtSubTitle,isRTL?Gravity.RIGHT:Gravity.LEFT);
        AndroidUtilities.ChangeGravitiy(txtSearch,isRTL?Gravity.RIGHT:Gravity.LEFT);
        txtSubTitle.setVisibility(hasSubtitle?VISIBLE:GONE);
        if (showContactAvatar) {
            layoutContactAvatar(hasSubtitle);
        } else if (eshowicon) {
            txtTitle.setPadding(AndroidUtilities.dp10+(isRTL?0:AndroidUtilities.dp(40)), 0, AndroidUtilities.dp10+(isRTL?AndroidUtilities.dp(40):0), hasSubtitle?AndroidUtilities.dp15:0);
            txtSubTitle.setPadding(AndroidUtilities.dp10+(isRTL?0:AndroidUtilities.dp(40)), AndroidUtilities.dp25, AndroidUtilities.dp10+(isRTL?AndroidUtilities.dp(40):0), 0);
        } else {
            txtTitle.setPadding(AndroidUtilities.dp10, 0, AndroidUtilities.dp10, hasSubtitle?AndroidUtilities.dp15:0);
            txtSubTitle.setPadding(AndroidUtilities.dp10, AndroidUtilities.dp25, AndroidUtilities.dp10, 0);
        }
        imgotherIcon.setVisibility(eshowothericon?VISIBLE:GONE);
        imgIcon.setVisibility(eshowicon ? VISIBLE : GONE);
        if (eshowicon) {
            applyFaIcon(imgIcon, icondrawble, titleColor != 0 ? titleColor : ForeGroundColor);
        }
        customviewframe.setVisibility(customview!=null?VISIBLE:GONE);

        AndroidUtilities.ChangeGravitiy(customviewframe,isRTL?Gravity.LEFT:Gravity.RIGHT);
        AndroidUtilities.ChangeGravitiy(imgotherIcon,isRTL?Gravity.RIGHT:Gravity.LEFT);
        AndroidUtilities.ChangeGravitiy(txtSearch,isRTL?Gravity.RIGHT:Gravity.LEFT);
        AndroidUtilities.ChangeGravitiy(imgGoSearchIcon,isRTL?Gravity.LEFT:Gravity.RIGHT);

        txtSearch.setVisibility(searchmode?VISIBLE:GONE);
        txtTitle.setVisibility(searchmode?GONE:VISIBLE);
        txtSubTitle.setVisibility(searchmode?GONE:VISIBLE);
        imgGoSearchIcon.setVisibility(searchmode?VISIBLE:GONE);
    }

    private void layoutContactAvatar(boolean hasSubtitle) {
        if (contactAvatarView == null) {
            return;
        }
        contactAvatarView.setVisibility(showContactAvatar && !searchmode ? VISIBLE : GONE);
        if (!showContactAvatar || searchmode) {
            return;
        }

        int avatarSize = AndroidUtilities.dp(com.dpouya.aitexty.ui.widgets.ContactAvatarView.SIZE_ACTIONBAR);
        int startEdge = eshowicon ? mActionBarSize + AndroidUtilities.dp5 : AndroidUtilities.dp10;
        int titleStart = startEdge + avatarSize + AndroidUtilities.dp8;

        FrameLayout.LayoutParams avatarLp = (FrameLayout.LayoutParams) contactAvatarView.getLayoutParams();
        avatarLp.width = avatarSize;
        avatarLp.height = avatarSize;
        avatarLp.gravity = Gravity.CENTER_VERTICAL | (isRTL ? Gravity.RIGHT : Gravity.LEFT);
        avatarLp.leftMargin = isRTL ? 0 : startEdge;
        avatarLp.rightMargin = isRTL ? startEdge : 0;
        avatarLp.topMargin = 0;
        avatarLp.bottomMargin = 0;
        contactAvatarView.setLayoutParams(avatarLp);

        if (eshowicon) {
            txtTitle.setPadding(
                    isRTL ? AndroidUtilities.dp10 : titleStart,
                    0,
                    isRTL ? titleStart : AndroidUtilities.dp10,
                    hasSubtitle ? AndroidUtilities.dp15 : 0);
            txtSubTitle.setPadding(
                    isRTL ? AndroidUtilities.dp10 : titleStart,
                    AndroidUtilities.dp25,
                    isRTL ? titleStart : AndroidUtilities.dp10,
                    0);
        } else {
            txtTitle.setPadding(
                    isRTL ? AndroidUtilities.dp10 : titleStart,
                    0,
                    isRTL ? titleStart : AndroidUtilities.dp10,
                    hasSubtitle ? AndroidUtilities.dp15 : 0);
            txtSubTitle.setPadding(
                    isRTL ? AndroidUtilities.dp10 : titleStart,
                    AndroidUtilities.dp25,
                    isRTL ? titleStart : AndroidUtilities.dp10,
                    0);
        }
    }
    public FaIconView getImgIcon() {
        return imgIcon;
    }

    public void showotherIcon(boolean show, FontAwesome.Icon icon){
        eshowothericon=show;
        iconotherdrawble=icon;

        recreateLayout();

    }

    public void setOnIconClick(OnClickListener onIconClick) {
        this.onIconClick = onIconClick;
    }
    public void setOnOtherIconClick(OnClickListener onotherIconClick) {
        this.onOtherIconClick = onotherIconClick;
    }

    public void getDefaults() {
        primaryColor = Theme.getColor(Theme.ACTIONBAR_COLOR);
        mActionBarSize = 50;
        riplebackgroundeffectResourse = R.drawable.actionbar_button_pressed;
    }

    public void showBackButton(boolean show) {
        showBackButton=show;
        if(isRTL){
            showIcon(show, FontAwesome.Icon.ARROW_RIGHT);
        }else{
            showIcon(show, FontAwesome.Icon.ARROW_LEFT);
        }
    }

    public EditText getTxtSearch() {
        return txtSearch;
    }
    public FaIconView getSearchIcon() {
        return imgGoSearchIcon;
    }

    public void setIsSearching(boolean isSearching) {
        applyFaIcon(imgGoSearchIcon,
                isSearching ? FontAwesome.Icon.CLOSE : FontAwesome.Icon.SEARCH,
                ForeGroundColor);
    }

    public void ClearButtons() {
        actionBarButtonListCell.ClearButtons();
    }
    private void changeTabsFont(TabLayout tabLayout) {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(this.typeFace);
                }
            }
        }
    }

    public void setDrawerMenuRtl(boolean drawerMenuRtl) {
        this.isRTL = drawerMenuRtl;
        recreateLayout();
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        txtTitle.setTextColor(titleColor);
        txtCenterTitle.setTextColor(titleColor);

    }

    public void setTypeFace(Typeface typeFace) {
        this.typeFace = typeFace;
        invalidate();
    }

    public void setSubTitleColor(int subTitlecolor) {
        this.subTitlecolor = subTitlecolor;
        txtSubTitle.setTextColor(subTitlecolor);
    }


    public void updateColors() {
        this.setBackgroundColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        this.setTitleColor(Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR));
        this.setForeGroundColor(Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR));
    }

    public void hideTitle() {
        setTitle("");
        setSubTitle("");
    }
}

