package top.zerotop.blog.serivce;

import java.util.List;

import top.zerotop.blog.entity.Article;

/**
 *@author 作者: zerotop
 *@createDate 创建时间: 2018年5月21日下午8:38:38
 */
public interface BlogService {

	public List<Article> listArticle();
	
	public Article getArticleById(long id);
	
	public int insertArticle(Article article);
}