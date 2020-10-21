package com.haibin.calendarviewproject;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.haibin.calendarviewproject.group.GroupRecyclerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 适配器
 * Created by huanghaibin on 2017/12/4.
 */

public class ArticleAdapter extends GroupRecyclerAdapter<String, Article> {


    private RequestManager mLoader;

    public ArticleAdapter(Context context) {
        super(context);
        mLoader = Glide.with(context.getApplicationContext());
        LinkedHashMap<String, List<Article>> map = new LinkedHashMap<>();
        List<String> titles = new ArrayList<>();
        map.put("Recommended Today", create(0));
        map.put("weekly hot spots", create(1));
        map.put("Highest Comment", create(2));
        titles.add("Recommended Today");
        titles.add("weekly hot spots");
        titles.add("Highest Comment");
        resetGroups(map,titles);
    }


    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new ArticleViewHolder(mInflater.inflate(R.layout.item_list_article, parent, false));
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, Article item, int position) {
        ArticleViewHolder h = (ArticleViewHolder) holder;
        h.mTextTitle.setText(item.getTitle());
        h.mTextContent.setText(item.getContent());
        mLoader.load(item.getImgUrl())
                .asBitmap()
                .centerCrop()
                .into(h.mImageView);
    }

    private static class ArticleViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextTitle,
                mTextContent;
        private ImageView mImageView;

        private ArticleViewHolder(View itemView) {
            super(itemView);
            mTextTitle = itemView.findViewById(R.id.tv_title);
            mTextContent = itemView.findViewById(R.id.tv_content);
            mImageView = itemView.findViewById(R.id.imageView);
        }
    }


    private static Article create(String title, String content, String imgUrl) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setImgUrl(imgUrl);
        return article;
    }

    private static List<Article> create(int p) {
        List<Article> list = new ArrayList<>();
        if (p == 0) {
            list.add(create("A magnitude 5.7 earthquake occurred in the Kemadek Islands in New Zealand with a focal depth of 10 kilometers",
                    "#Earthquake news# China Earthquake Network officially determined that at 08:08 on December 4, a magnitude 5.7 earthquake occurred in the Kermadeke Islands (32.82 degrees south latitude, 178.73 degrees west longitude) with a focal depth of 10 kilometers.",
                    "http://cms-bucket.nosdn.127.net/catchpic/2/27/27e2ce7fd02e6c096e21b1689a8a3fe9.jpg?imageView&thumbnail=550x0"));
            list.add(create("Russia's wrongdoing\"back pot man\" Russia and the United States fall into the\"post truth\"vortex",
                    "It's shockingly bad, but Trump is not to blame. Russian Prime Minister Dmitry Medvedev recently said this when talking about Russia-US relations. Russia has recently been repeatedly accused by the United States of virulent and linked with Western countries. Attack. Some international public opinion believes that Russia has become a back-up man, and Russia itself has publicly criticized the United States. In the exchanges between Russia and the United States, the truth seems to have become less important.",
                    "http://cms-bucket.nosdn.127.net/catchpic/c/c8/c8b0685089258b82f3ca1997def78d8d.png?imageView&thumbnail=550x0"));
            list.add(create("Chinese companies invest in Brazil are supported British media: Brazilians are grateful \"keep their jobs\"",
                    "Reference News reported on December 4 that the British media said that the port of Aso near Rio de Janeiro was once called 'the highway to China' by Eke Batista. More than 10 years ago, this disgraced Brazilian The former richest man created this super port. After the commodity boom ended, almost none of his business empire in Brazil survived and went bankrupt in 2014. However, one project that has continued to flourish since then is Port Aso.",
                    "http://cms-bucket.nosdn.127.net/catchpic/8/8b/8ba2d19b7f63efc5cf714960d5edd2c3.jpg?imageView&thumbnail=550x0"));
            list.add(create("U.S. TV reporter was suspended for four weeks for misreporting Flynn news",
                    "[Global Network Report] According to a report by Russian Satellite Network on December 3, ABC TV reporter Brian Russell was temporarily suspended due to a mistake in a news report on Michael Flynn, the former national security adviser to the US President.",
                    "http://cms-bucket.nosdn.127.net/5d18566fde70407b9cc3a728822115c320171203133214.jpeg?imageView&thumbnail=550x0"));
            list.add(create("It is expected to be listed in March next year, revealing the new Audi Q5L without spy photos",
                    "With the previous new generation of domestically produced Audi Q5L in the catalog of the Ministry of Industry and Information Technology, the recently exposed test cars have basically lost their camouflage, but the official launch will have to wait until March 2018. Judging from the newly exposed interiors, the wheelbase has been lengthened. The space in the back row has been greatly improved.",
                    "http://cms-bucket.nosdn.127.net/eda9ca222352470190c4f0d6b9a8c29420171201160854.jpeg?imageView&thumbnail=550x0"));
        } else if (p == 1) {
            list.add(create(
                    "In 2019, the production base of electric coffee complete vehicles settled in Shaoxing, Zhejiang",
                    "Netease Auto reported on November 30 that at the Guangzhou Auto Show two weeks ago, Dianca released its first electric car, EV10, with an official guide price of 133,800 to 141,800, and a retail price of 59,800 to 67,800 after deducting subsidies. Aside from the vehicle itself, the industry’s attention is focused on the core members of this new car manufacturer. The three veterans of the SAIC Volkswagen team - Zhang Hailiang, Xiang Dongping, and Niu Shengfu worked together for 957 days. Built a car that can be marketed.",
                    "http://cms-bucket.nosdn.127.net/674c392123254bb69bdd9227442965eb20171129203658.jpeg?imageView&thumbnail=550x0"));
            list.add(create(
                    "2017 is coming to an end, is Apple's big bet on ARkit okay?",
                    "Google has launched AR glasses, ARCore platform and Project Tango, which is applied to mobile phones, and Facebook has also launched AR development platforms and tools. As for Apple, AR is the top priority for development. In the new iPhone8 and iPhoneX, The rear camera is calibrated specifically for AR, and the front camera also adds a depth sensor that can bring better AR effects.",
                    "http://cms-bucket.nosdn.127.net/catchpic/7/76/76135ac5d3107a1d5ba11a8ee2fc7e27.jpg?imageView&thumbnail=550x0"));
            list.add(create(
                    "Amazon CTO: We want to make humans the center of robots!",
                    "Smartphone evangelists and app enthusiasts who believe that app downloads will make the world a better place will feel uncomfortable at the AWS re:Invent conference. Werner Vogels, CTO of Amazon Web Services, said that none of this To achieve democratization of information.",
                    "http://cms-bucket.nosdn.127.net/ddb758f16a7d4aa3aa422ec385fc3e5020171204081818.jpeg?imageView&thumbnail=550x0"));
            list.add(create(
                    "There are Tesla owners who want to use free charging piles for mining, but this may not work",
                    "In a group of Tesla owners on the social network Facebook, someone thought they could try to assemble the mining machine by themselves, put it in the Tesla trunk, plug in the car battery, and then park the car to supercharge Near the pile, you can use the free electricity to mine.",
                    "http://crawl.nosdn.127.net/nbotreplaceimg/4ce9c743e6c02f6777d22278e2ef8bc3/2b33e32532db204fe207693c82719660.jpg"));
        } else if (p == 2) {
            list.add(create("A magnitude 5.7 earthquake occurred in the Kemadek Islands in New Zealand with a focal depth of 10 kilometers",
                    "#Earthquake news# China Earthquake Network officially determined that at 08:08 on December 4, a magnitude 5.7 earthquake occurred in the Kermadeke Islands (32.82 degrees south latitude, 178.73 degrees west longitude) with a focal depth of 10 kilometers.",
                    "http://cms-bucket.nosdn.127.net/catchpic/2/27/27e2ce7fd02e6c096e21b1689a8a3fe9.jpg?imageView&thumbnail=550x0"));
            list.add(create("Russia's wrongdoing\"back pot man\" Russia and the United States fall into the\"post truth\"vortex",
                    "It's shockingly bad, but Trump is not to blame. Russian Prime Minister Dmitry Medvedev recently said this when talking about Russia-US relations. Russia has recently been repeatedly accused by the United States of virulent and linked with Western countries. Attack. Some international public opinion believes that Russia has become a back-up man, and Russia itself has publicly criticized the United States. In the exchanges between Russia and the United States, the truth seems to have become less important.",
                    "http://cms-bucket.nosdn.127.net/catchpic/c/c8/c8b0685089258b82f3ca1997def78d8d.png?imageView&thumbnail=550x0"));
            list.add(create("Chinese companies invest in Brazil are supported British media: Brazilians are grateful \"keep their jobs\"",
                    "Reference News reported on December 4 that the British media said that the port of Aso near Rio de Janeiro was once called 'the highway to China' by Eke Batista. More than 10 years ago, this disgraced Brazilian The former richest man created this super port. After the commodity boom ended, almost none of his business empire in Brazil survived and went bankrupt in 2014. However, one project that has continued to flourish since then is Port Aso.",
                    "http://cms-bucket.nosdn.127.net/catchpic/8/8b/8ba2d19b7f63efc5cf714960d5edd2c3.jpg?imageView&thumbnail=550x0"));
            list.add(create("U.S. TV reporter was suspended for four weeks for misreporting Flynn news",
                    "[Global Network Report] According to a report by Russian Satellite Network on December 3, ABC TV reporter Brian Russell was temporarily suspended due to a mistake in a news report on Michael Flynn, the former national security adviser to the US President.",
                    "http://cms-bucket.nosdn.127.net/5d18566fde70407b9cc3a728822115c320171203133214.jpeg?imageView&thumbnail=550x0"));
            list.add(create("It is expected to be listed in March next year, revealing the new Audi Q5L without spy photos",
                    "With the previous new generation of domestically produced Audi Q5L in the catalog of the Ministry of Industry and Information Technology, the recently exposed test cars have basically lost their camouflage, but the official launch will have to wait until March 2018. Judging from the newly exposed interiors, the wheelbase has been lengthened. The space in the back row has been greatly improved.",
                    "http://cms-bucket.nosdn.127.net/eda9ca222352470190c4f0d6b9a8c29420171201160854.jpeg?imageView&thumbnail=550x0"));
        }


        return list;
    }
}
